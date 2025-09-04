package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

class Message {
    private final String message;

    Message(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

interface IPublisher {
    void publish(Integer topicId, Message message);
}

interface ISubscriber {
    void onMessage(Message message) throws InterruptedException;
}

class SimpleSubscriber implements ISubscriber {
    private final String id;
    public SimpleSubscriber(String id) {
        this.id = id;
    }

    @Override
    public void onMessage(Message message) {
        // Processing the received message.
        System.out.println("Subscriber " + id + " received: " + message.getMessage());
        // Simulate processing delay if desired
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

class SimplePublisher implements IPublisher {
    private final String id;
    private final KafkaController kafkaController;

    public SimplePublisher(String id, KafkaController kafkaController) {
        this.id = id;
        this.kafkaController = kafkaController;
    }
    @Override
    public void publish(Integer topicId, Message message) {
        kafkaController.publish(this, topicId, message);
        System.out.println("Publisher " + id + " published: " + message.getMessage() + " to topic " + topicId);
    }
}

class TopicSubscriber {
    private final Topic topic;
    private final ISubscriber subscriber;
    private final AtomicInteger offset;

    TopicSubscriber(Topic topic, ISubscriber subscriber) {
        this.topic = topic;
        this.subscriber = subscriber;
        this.offset = new AtomicInteger(0);
    }

    public Topic getTopic() {
        return topic;
    }

    public ISubscriber getSubscriber() {
        return subscriber;
    }

    public AtomicInteger getOffset() {
        return offset;
    }
}

class Topic {
    private final String topicName;
    private final Integer topicId;
    private final List<Message> messageList;

    Topic(String topicName, Integer topicId) {
        this.topicName = topicName;
        this.topicId = topicId;
        this.messageList = new ArrayList<>();
    }

    public String getTopicName() {
        return topicName;
    }

    public Integer getTopicId() {
        return topicId;
    }

    public synchronized void addMessage(Message newMessage) {
        messageList.add(newMessage);
    }

    public synchronized List<Message> getMessageList() {
        return messageList;
    }
}

class TopicSubscriberController implements Runnable {
    private final TopicSubscriber topicSubscriber;

    TopicSubscriberController(TopicSubscriber topicSubscriber) {
        this.topicSubscriber = topicSubscriber;
    }

    @Override
    public void run() {
        Topic topic = topicSubscriber.getTopic();
        ISubscriber subscriber = topicSubscriber.getSubscriber();
        while(true) {
            Message message = null;
            synchronized (topicSubscriber) {
                while(topicSubscriber.getOffset().get() >= topic.getMessageList().size()) {
                    try {
                        topicSubscriber.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                int currentOffset = topicSubscriber.getOffset().getAndIncrement();
                message = topic.getMessageList().get(currentOffset);
            }

            subscriber.onMessage(message);
        }
    }
}

class KafkaController {
    private final Map<Integer, Topic> topics;
    private final Map<Integer, List<TopicSubscriber>> topicSubscribers;
    private final ExecutorService executorService;
    private final AtomicInteger topicIdCounter;

    KafkaController() {
        this.topics = new ConcurrentHashMap<>();
        this.topicSubscribers = new ConcurrentHashMap<>();
        this.executorService = Executors.newCachedThreadPool();
        this.topicIdCounter = new AtomicInteger(0);
    }

    public Topic createTopic(String topicName) {
        int topicId = this.topicIdCounter.getAndIncrement();
        Topic topic = new Topic(topicName, topicId);
        this.topics.put(topicId, topic);
        this.topicSubscribers.put(topicId, new CopyOnWriteArrayList<>());
        return topic;
    }

    public void subscribe(ISubscriber subscriber, Integer topicId) {
        Topic topic = this.topics.get(topicId);
        if (topic == null) {
            System.err.println("Topic with id " + topicId + " does not exist");
            return;
        }
        TopicSubscriber ts = new TopicSubscriber(topic, subscriber);
        executorService.submit(new TopicSubscriberController(ts));
    }

    public void publish(IPublisher publisher, Integer topicId, Message message) {
        Topic topic = this.topics.get(topicId);
        if (topic == null) {
            System.err.println("Topic with id " + topicId + " does not exist");
            return;
        }
        List<TopicSubscriber> ts = topicSubscribers.get(topicId);
        for(TopicSubscriber topicSubscriber: ts) {
            synchronized (topicSubscriber) {
                topicSubscribers.notify();
            }
        }
    }

    public void resetOffset(Integer topicId, ISubscriber subscriber, int newOffset) {
        List<TopicSubscriber> ts = topicSubscribers.get(topicId);
        for(TopicSubscriber topicSubscriber: ts) {
            if(topicSubscriber.getSubscriber().equals(subscriber)) {
                topicSubscriber.getOffset().set(newOffset);
                synchronized (topicSubscriber) {
                    ts.notify();
                }
                break;
            }
        }
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }

}

public class Main {
    public static void main(String[] args) {
        KafkaController kafkaController = new KafkaController();


        // Create topics.
        Topic topic1 = kafkaController.createTopic("Topic1");
        Topic topic2 = kafkaController.createTopic("Topic2");

        // Create subscribers.
        ISubscriber subscriber1 = new SimpleSubscriber("Subscriber1");
        ISubscriber subscriber2 = new SimpleSubscriber("Subscriber2");
        ISubscriber subscriber3 = new SimpleSubscriber("Subscriber3");


        // Subscribe: subscriber1 subscribes to both topics,
        // subscriber2 subscribes to topic1, and subscriber3 subscribes to topic2.
        kafkaController.subscribe(subscriber1, topic1.getTopicId());
        kafkaController.subscribe(subscriber1, topic2.getTopicId());
        kafkaController.subscribe(subscriber2, topic1.getTopicId());
        kafkaController.subscribe(subscriber3, topic2.getTopicId());


        // Create publishers.
        IPublisher publisher1 = new SimplePublisher("Publisher1", kafkaController);
        IPublisher publisher2 = new SimplePublisher("Publisher2", kafkaController);


        // Publish some messages.
        publisher1.publish(topic1.getTopicId(), new Message("Message m1"));
        publisher1.publish(topic1.getTopicId(), new Message("Message m2"));
        publisher2.publish(topic2.getTopicId(), new Message("Message m3"));

        // Allow time for subscribers to process messages.
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        publisher2.publish(topic2.getTopicId(), new Message("Message m4"));
        publisher1.publish(topic1.getTopicId(), new Message("Message m5"));


        // Reset offset for subscriber1 on topic1 (for example, to re-process messages).
        kafkaController.resetOffset(topic1.getTopicId(), subscriber1, 0);


        // Allow some time before shutting down.
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        kafkaController.shutdown();
    }
}