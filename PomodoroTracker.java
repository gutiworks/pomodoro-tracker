import java.io.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class PomodoroTracker {

    private static final int POMODOROS_PER_DAY = 8;
    private static final int POMODORO_MINUTES = 30;
    private static final String[] DAYS = {
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    };

    private static final String SAVE_FILE = "pomodoro.json";
    private static final Gson gson = new Gson();

    // Store weeks in memory: weekNumber -> day -> pomodoros
    private static Map<Integer, Map<String, boolean[]>> allWeeks = new HashMap<>();
    private static int currentWeek = 1;
    private static int currentDayIndex = 0;

    public static void main(String[] args) throws IOException {
        // load existing data if available
        loadData();

        // define your start Monday (CHANGE THIS to your real start date)
        LocalDate startDate = LocalDate.of(2025, 9, 29);
        LocalDate today = LocalDate.now();

        // calculate how many weeks passed since startDate
        long weeksPassed = ChronoUnit.WEEKS.between(startDate, today);
        currentWeek = (int) weeksPassed + 1; // +1 so first week = 1

        ensureWeekExists(currentWeek);

        // set cursor to today
        currentDayIndex = today.getDayOfWeek().getValue() - 1; // Monday=0

        System.out.println("(w=up, s=down, x=add, r=remove, a=prev week, d=next week, q=quit)");

        while (true) {
            printWeek();
            int key = System.in.read();

            switch (key) {
                case 'w' -> moveUp();
                case 's' -> moveDown();
                case 'x' -> { addPomodoro(); saveData(); }
                case 'r' -> { removePomodoro(); saveData(); }
                case 'a' -> movePrevWeek();
                case 'd' -> moveNextWeek();
                case 'q' -> {
                    saveData();
                    System.out.println("Progress saved. Exiting...");
                    return;
                }
            }
            // clear buffer (remove extra Enter key)
            if (System.in.available() > 0) {
                System.in.skip(System.in.available());
            }
        }
    }

    private static void ensureWeekExists(int weekNum) {
        if (!allWeeks.containsKey(weekNum)) {
            Map<String, boolean[]> week = new LinkedHashMap<>();
            for (String day : DAYS) {
                week.put(day, new boolean[POMODOROS_PER_DAY]);
            }
            allWeeks.put(weekNum, week);
        }
    }

    private static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private static void printWeek() {
        clearScreen();
        Map<String, boolean[]> week = allWeeks.get(currentWeek);
        System.out.println("==== (Week " + currentWeek + ") ====");

        for (int i = 0; i < DAYS.length; i++) {
            String day = DAYS[i];
            boolean[] pomodoros = week.get(day);

            StringBuilder sb = new StringBuilder();
            int dayCount = 0;
            for (boolean p : pomodoros) {
                if (p) dayCount++;
                sb.append(p ? "[X] " : "[ ] ");
            }

            int dayMinutes = dayCount * POMODORO_MINUTES;
            int dayHours = dayMinutes / 60;
            int dayMins = dayMinutes % 60;

            if (i == currentDayIndex) {
                System.out.printf("-> %-10s %s  %dh %dm%n", day, sb.toString(), dayHours, dayMins);
            } else {
                System.out.printf("   %-10s %s  %dh %dm%n", day, sb.toString(), dayHours, dayMins);
            }
        }

        // Week total
        int totalMinutes = getWeekMinutes(currentWeek);
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        //System.out.println("\nWeek total: " + hours + "h " + minutes + "m");

        // Last 4 weeks total
        int fourWeeksMinutes = getLastFourWeeksMinutes(currentWeek);
        int fwHours = fourWeeksMinutes / 60;
        int fwMins = fourWeeksMinutes % 60;
        System.out.println("\nPast 4 weeks: " + fwHours + "h " + fwMins + "m" + " / 100h");
    }

    private static int getWeekMinutes(int weekNum) {
        Map<String, boolean[]> week = allWeeks.get(weekNum);
        int total = 0;
        for (boolean[] pomodoros : week.values()) {
            for (boolean p : pomodoros) {
                if (p) total += POMODORO_MINUTES;
            }
        }
        return total;
    }

    private static int getLastFourWeeksMinutes(int currentWeek) {
        int total = 0;
        for (int w = currentWeek; w > currentWeek - 4 && w > 0; w--) {
            ensureWeekExists(w);
            total += getWeekMinutes(w);
        }
        return total;
    }

    private static void moveUp() {
        currentDayIndex = (currentDayIndex - 1 + DAYS.length) % DAYS.length;
    }

    private static void moveDown() {
        currentDayIndex = (currentDayIndex + 1) % DAYS.length;
    }

    private static void movePrevWeek() {
        currentWeek = Math.max(1, currentWeek - 1);
        ensureWeekExists(currentWeek);
    }

    private static void moveNextWeek() {
        currentWeek++;
        ensureWeekExists(currentWeek);
    }

    private static void addPomodoro() {
        Map<String, boolean[]> week = allWeeks.get(currentWeek);
        String day = DAYS[currentDayIndex];
        boolean[] pomodoros = week.get(day);

        for (int i = 0; i < POMODOROS_PER_DAY; i++) {
            if (!pomodoros[i]) {
                pomodoros[i] = true;
                return;
            }
        }
    }

    private static void removePomodoro() {
        Map<String, boolean[]> week = allWeeks.get(currentWeek);
        String day = DAYS[currentDayIndex];
        boolean[] pomodoros = week.get(day);

        for (int i = POMODOROS_PER_DAY - 1; i >= 0; i--) {
            if (pomodoros[i]) {
                pomodoros[i] = false;
                return;
            }
        }
        System.out.println("No pomodoros to remove in " + day + " (Week " + currentWeek + ")");
    }

    // === Persistence Methods ===
    private static void saveData() {
        try (FileWriter writer = new FileWriter(SAVE_FILE)) {
            gson.toJson(allWeeks, writer);
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }

    private static void loadData() {
        File file = new File(SAVE_FILE);
        if (!file.exists()) return;

        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<Integer, Map<String, boolean[]>>>(){}.getType();
            allWeeks = gson.fromJson(reader, type);
        } catch (IOException e) {
            System.out.println("Error loading data: " + e.getMessage());
        }
    }
}
