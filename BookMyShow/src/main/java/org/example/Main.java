package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

// enum
enum SeatType {
    GOLD,
    SILVER,
    NORMAL
}

// interfaces
//interface PaymentStrategy {
//    void doPayment();
//}

interface SeatLockProvider {
    void lockSeats(Show show, List<Seat> seatList, User user);
    void unlockSeats(Show show, List<Seat> seatList, User user);
    boolean validateLock(Show show, Seat seat, User user);
    List<Seat> getLockedSeats(Show show);
}

// concrete classes
class User {
    private final String id;
    private final String userName;

    User(String id, String userName) {
        this.id = id;
        this.userName = userName;
    }

    public String getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }
}

class Theatre {
    private final String id;
    private final String name;
    private final List<Screen> screens;
    private final List<Show> shows;
    private final String location;

    Theatre(String id, String name, List<Screen> screens, List<Show> shows, String location) {
        this.id = id;
        this.name = name;
        this.screens = screens;
        this.shows = shows;
        this.location = location;
    }

    public List<Show> getShows() {
        return shows;
    }

    public String getId() {
        return id;
    }
}

class Screen {
    private final String id;
    private final List<Show> shows;
    private final List<Seat> seats;

    Screen(String id, List<Show> shows, List<Seat> seats) {
        this.id = id;
        this.shows = shows;
        this.seats = seats;
    }
}

class Show {
    private final String moveName;
    private final long startTime;
    private final long endTime;
    private final List<Seat> seats;
    private final Theatre theatre;
    private List<Seat> bookedSeats;

    Show(String moveName, long startTime, long endTime, List<Seat> seats, Theatre theatre) {
        this.moveName = moveName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.seats = seats;
        this.theatre = theatre;
        this.bookedSeats = new ArrayList<>();
    }

    public Theatre getTheatre() {
        return theatre;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public void bookSeat(Seat seat) {
        this.bookedSeats.add(seat);
    }

    public List<Seat> getBookedSeats() {
        return bookedSeats;
    }
}

class Seat {
    private final String id;
    private final SeatType seatType;

    Seat(String id, SeatType seatType) {
        this.id = id;
        this.seatType = seatType;
    }
}

class SeatLock {
    private final Seat seat;
    private final Show show;
    private final long timeOut;
    private final long lockedTime;
    private final User lockedBy;

    SeatLock(Seat seat, Show show, long timeOut, long lockedTime, User lockedBy) {
        this.seat = seat;
        this.show = show;
        this.timeOut = timeOut;
        this.lockedTime = lockedTime;
        this.lockedBy = lockedBy;
    }

    public User getLockedBy() {
        return lockedBy;
    }

    public boolean isLockExpired() {
        long currentMillis = System.currentTimeMillis();
        return lockedTime + timeOut < currentMillis;
    }
}

class TheatreService {
    private final List<Theatre> theatres;

    TheatreService(List<Theatre> theatres) {
        this.theatres = theatres;
    }

    public void addTheatre (Theatre theatre) {
        theatres.add(theatre);
    }

    public List<Show> allShowsInTheatre(String theatreId) {
        Optional<Theatre> optionalTheatre = theatres
                .stream()
                .filter(theatre -> theatre.getId().equals(theatreId))
                .findFirst();

        if(optionalTheatre.isEmpty()) {
            throw  new RuntimeException("No theatre");
        }

        return optionalTheatre.get().getShows();
    }

    public List<Theatre> getAllTheatre() {
        return this.theatres;
    }
}

class UserService {
    private final List<User> users;

    UserService(List<User> users) {
        this.users = users;
    }

    public void addUser(User user) {
        this.users.add(user);
    }

    public Optional<User> getUserById(String userId) {
        return users.stream().filter(user -> user.getId().equals(userId)).findFirst();
    }

}

class SeatLockProviderImplementation implements SeatLockProvider {

    private final Map<Show, Map<Seat, SeatLock>> locks;

    SeatLockProviderImplementation() {
        locks = new ConcurrentHashMap<>();
    }

    @Override
    public void lockSeats(final Show show, final List<Seat> seatList, User user) {
        Map<Seat, SeatLock> seatLocks = locks.computeIfAbsent(show, s -> new ConcurrentHashMap<>());
        synchronized (seatLocks) {
            for(Seat seat: seatList) {
                if(seatLocks.containsKey(seat)) {
                    if(!seatLocks.get(seat).isLockExpired()) {
                        throw new RuntimeException("Seat already locked...");
                    }
                }
            }

            for(Seat seat: seatList) {
                SeatLock seatLock = new SeatLock(seat, show, 5000, System.currentTimeMillis(), user);
                seatLocks.put(seat, seatLock);
            }
        }
    }

    @Override
    public void unlockSeats(Show show, List<Seat> seatList, User user) {
        Map<Seat, SeatLock> seatLocks = locks.computeIfAbsent(show, s -> new ConcurrentHashMap<>());
        synchronized (seatLocks) {
            boolean anyUnauthorizedRequest = false;
            for(Seat seat: seatList) {
                SeatLock currentSeatLock = seatLocks.get(seat);
                if(currentSeatLock != null &&  !currentSeatLock.getLockedBy().equals(user)) {
                    anyUnauthorizedRequest = true;
                } else {
                    seatLocks.remove(seat);
                }
            }

            if(anyUnauthorizedRequest) {
                throw new RuntimeException("Unauthorized user tried to release!!!");
            }
        }
    }

    @Override
    public boolean validateLock(Show show, Seat seat, User user) {
        Map<Seat, SeatLock> seatLocks = locks.get(show);
        if(seatLocks == null) return false;
        synchronized (seatLocks) {
            SeatLock seatLock = seatLocks.get(seat);
            return (seatLock.getLockedBy().equals(user));
        }
    }

    @Override
    public List<Seat> getLockedSeats(Show show) {
        synchronized (show) {
            Map<Seat, SeatLock> seatLocks = locks.computeIfAbsent(show, s -> new ConcurrentHashMap<>());
            return seatLocks.keySet().stream().toList();
        }
    }
}

class BookMyShow {
    private final TheatreService theatreService;
    private final UserService userService;
    private final SeatLockProvider seatLockProvider;


    BookMyShow(TheatreService theatreService, UserService userService, SeatLockProvider seatLockProvider) {
        this.theatreService = theatreService;
        this.userService = userService;
        this.seatLockProvider = seatLockProvider;
    }

    public List<Theatre> getAllTheatre() {
        return theatreService.getAllTheatre();
    }

    public List<Show> getAllShows(Theatre theatre) {
        return theatreService.allShowsInTheatre(theatre.getId());
    }

    public List<Seat> getAllAvailableSeats(Show show) {
        List<Seat> seats = show.getSeats();
        List<Seat> inValidSeats = show.getBookedSeats();
        List<Seat> lockedSeats = seatLockProvider.getLockedSeats(show);

        inValidSeats.addAll(lockedSeats);

        return seats.stream().filter(seat -> !inValidSeats.contains(seat)).toList();
    }

    public void doBooking(Show show, List<Seat> seats, String userId) {
        Optional<User> user = userService.getUserById(userId);

        if(user.isEmpty()) {
            throw new RuntimeException("No User found..");
        }

        seatLockProvider.lockSeats(show, seats, user.get());
        boolean paymentSuccess = processPayment();

        if (paymentSuccess) {
            for(Seat seat: seats) {
                if(!seatLockProvider.validateLock(show, seat, user.get())) {
                    // initiate refund...
                    throw new RuntimeException("Lock expired");
                }
            }
            for (Seat seat : seats) {
                show.bookSeat(seat);
            }
            // unlock not needed, seats are booked permanently
            // create ticket
        } else {
            cancelBooking(show, seats, user.get());
        }

    }

    private void cancelBooking(Show show, List<Seat> seats, User user) {
        seatLockProvider.unlockSeats(show, seats, user);
    }

    private boolean processPayment() {
        try {
            System.out.println("Payment started...");
            Thread.sleep(5000);
            System.out.println("Payment ended...");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
}

public class Main {
    public static void main(String[] args) {

    }
}