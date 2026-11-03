import java.util.List;
import java.util.Random;

/**
 * Runs the Candy Courier game: the main menu, travel, orders, inventory and the day cycle.
 */
public class Game {
    private static final String[] DIRECTION_NAMES = {
        "Up", "Down", "Left", "Right", "Up-Left", "Up-Right", "Down-Left", "Down-Right"};
    private static final int[][] DIRECTION_DELTAS = {
        {0, 1}, {0, -1}, {-1, 0}, {1, 0}, {-1, 1}, {1, 1}, {-1, -1}, {1, -1}};

    private final InputHelper input;
    private final World world;
    private Player player;
    private int day;
    private boolean gameOver;
    private boolean quit;

    /**
     * Creates a game and stocks the factories for day 1.
     *
     * <p><b>Post-condition:</b> the world is populated and ready; the player is created in {@link #start()}.</p>
     *
     * @param input  the console input helper
     * @param random the random generator used to stock the factories
     */
    public Game(InputHelper input, Random random) {
        this.input = input;
        this.world = new World();
        this.day = 1;
        GameData.populate(world, random);
        world.applyRank(Rank.ZERO_STAR);
        world.refreshFactories();
    }

    /**
     * Starts the game: asks for the player's name and runs the main menu until the player quits
     * or the game ends.
     *
     * <p><b>Post-condition:</b> the final stats of the player have been displayed.</p>
     */
    public void start() {
        System.out.println("=====================================");
        System.out.println("          C A N D Y  C O U R I E R   ");
        System.out.println("=====================================");
        String name = input.readNonEmptyLine("Enter your name: ");
        player = new Player(name);
        System.out.println("Welcome, " + name + "! Deliver candies to climb the ranks and become a 5-star courier.");

        while (!gameOver && !quit) {
            System.out.println();
            System.out.println("========== Day " + day + " ==========");
            world.display(player);
            System.out.println("1. Travel");
            System.out.println("2. Search Candies");
            System.out.println("3. View Orders");
            System.out.println("4. View Inventory");
            System.out.println("5. View Player Info");
            System.out.println("6. Quit Game");
            int choice = input.readInt("Choose: ", 1, 6);
            switch (choice) {
                case 1 -> travel();
                case 2 -> searchCandies();
                case 3 -> viewOrders();
                case 4 -> viewInventory();
                case 5 -> printPlayerInfo();
                default -> quitGame();
            }
        }
        if (gameOver) {
            endGame();
        }
    }

    /**
     * Lets the player roam the world until the player exits travel mode.
     *
     * <p><b>Post-condition:</b> the player's position, steps, inventory and XP may have changed.</p>
     */
    private void travel() {
        boolean traveling = true;
        while (traveling && !gameOver) {
            System.out.println();
            world.display(player);
            System.out.println("Position: " + world.describeLocation(player.getX(), player.getY()));
            int moves = player.getRank().isDiagonalAllowed() ? 8 : 4;
            System.out.println("--- Travel ---");
            for (int i = 0; i < moves; i++) {
                System.out.println((i + 1) + ". Move " + DIRECTION_NAMES[i]);
            }
            int interactOption = moves + 1;
            int exitOption = moves + 2;
            System.out.println(interactOption + ". Interact with the building on this tile");
            System.out.println(exitOption + ". Exit travel");
            int choice = input.readInt("Choose: ", 1, exitOption);
            if (choice == exitOption) {
                traveling = false;
            } else if (choice == interactOption) {
                interactWithCurrentTile(true);
            } else {
                move(choice - 1);
            }
        }
    }

    /**
     * Tries to move the player one tile and reports the result.
     *
     * <p><b>Post-condition:</b> if the destination is inside the world the player moves and takes a step;
     * otherwise the player stays and a message explains why.</p>
     *
     * @param direction the index of the direction in the direction tables
     */
    private void move(int direction) {
        int newX = player.getX() + DIRECTION_DELTAS[direction][0];
        int newY = player.getY() + DIRECTION_DELTAS[direction][1];
        if (!world.isInBounds(newX, newY)) {
            System.out.println("You can't go " + DIRECTION_NAMES[direction]
                    + ": that is beyond the edge of the world. You are still at "
                    + world.describeLocation(player.getX(), player.getY()) + ".");
            return;
        }
        player.moveTo(newX, newY);
        System.out.println("You moved " + DIRECTION_NAMES[direction] + " to " + world.describeLocation(newX, newY) + ".");
        interactWithCurrentTile(false);
    }

    /**
     * Starts the interaction with the building under the player, if there is one.
     *
     * @param forced true if the player asked to interact (shows messages even when nothing happens);
     *               false if the player just landed on the tile
     */
    private void interactWithCurrentTile(boolean forced) {
        Building building = world.getTile(player.getX(), player.getY()).getBuilding();
        if (building == null) {
            if (forced) {
                System.out.println("There is no building on this tile.");
            }
            return;
        }
        if (!forced && !building.promptsOnVisit()) {
            System.out.println("You pass by " + building.getName() + " (already delivered).");
            return;
        }
        Rank before = player.getRank();
        boolean endDay = building.interact(player, input);
        afterInteraction(before, endDay);
    }

    /**
     * Handles everything that can follow an interaction: a new day, a promotion and the end of the game.
     *
     * @param before the player's rank before the interaction
     * @param endDay true if the player chose to end the day
     */
    private void afterInteraction(Rank before, boolean endDay) {
        if (endDay) {
            advanceDay();
        }
        if (player.getRank() != before) {
            handleRankUp(before);
        }
        if (player.getRank() == Rank.FIVE_STAR && world.allHousesDelivered()) {
            gameOver = true;
        }
    }

    /**
     * Starts the next day: all factories are restocked.
     *
     * <p><b>Post-condition:</b> day increases by 1 and no usable factory slot is vacant.</p>
     */
    private void advanceDay() {
        world.refreshFactories();
        day++;
        System.out.println("You call it a day. All factories have restocked with fresh candy.");
        System.out.println("A new day begins: Day " + day + ".");
    }

    /**
     * Announces a promotion and expands the world and factories.
     *
     * @param before the rank the player had before being promoted
     */
    private void handleRankUp(Rank before) {
        Rank now = player.getRank();
        world.applyRank(now);
        System.out.println();
        System.out.println("*** RANK UP! You are now a " + now + " courier! ***");
        System.out.println("The world is now " + now.getWorldSize() + "x" + now.getWorldSize() + ".");
        System.out.println("Factory candy grids are now " + now.getProductionSize() + "x" + now.getProductionSize()
                + " (new slots stay empty until the next day).");
        System.out.println("You can now carry up to " + now.getInventoryCapacity() + " candies.");
        if (now.isDiagonalAllowed() && !before.isDiagonalAllowed()) {
            System.out.println("You can now move diagonally!");
        }
        for (Factory f : world.getFactories()) {
            if (f.getUnlockStars() > before.getStars() && f.getUnlockStars() <= now.getStars()) {
                System.out.println(f.getName() + " has opened at " + world.describeLocation(f.getX(), f.getY()) + ".");
            }
        }
        if (now != Rank.FIVE_STAR) {
            System.out.println("New houses with new orders have appeared at the edge of the world.");
        }
    }

    /**
     * Lets the player look up which accessible factories produce a chosen candy (type and flavor).
     */
    private void searchCandies() {
        CandyType[] types = CandyType.values();
        System.out.println("--- Search Candies ---");
        for (int i = 0; i < types.length; i++) {
            System.out.println((i + 1) + ". " + types[i]);
        }
        System.out.println((types.length + 1) + ". Back");
        int typeChoice = input.readInt("Which kind of candy are you looking for? ", 1, types.length + 1);
        if (typeChoice == types.length + 1) {
            return;
        }
        CandyType type = types[typeChoice - 1];
        Candy wanted;
        if (type.hasFlavors()) {
            Flavor[] flavors = Flavor.values();
            for (int i = 0; i < flavors.length; i++) {
                System.out.println((i + 1) + ". " + flavors[i]);
            }
            System.out.println((flavors.length + 1) + ". Back");
            int flavorChoice = input.readInt("Which flavor? ", 1, flavors.length + 1);
            if (flavorChoice == flavors.length + 1) {
                return;
            }
            wanted = new Candy(type, flavors[flavorChoice - 1]);
        } else {
            wanted = Candy.plain(type);
        }
        List<Factory> found = world.getFactoriesProducing(wanted);
        if (found.isEmpty()) {
            System.out.println("No factory you can reach produces " + wanted + " yet.");
            return;
        }
        System.out.println(wanted + " can be found in:");
        for (Factory f : found) {
            System.out.println("  " + f.getName() + " at " + world.describeLocation(f.getX(), f.getY()));
        }
    }

    /**
     * Shows the orders of all houses the player can currently reach.
     */
    private void viewOrders() {
        System.out.println("===== Orders =====");
        for (House h : world.getVisibleHouses()) {
            System.out.println(h.getName() + " at " + world.describeLocation(h.getX(), h.getY()) + " - "
                    + (h.isDelivered() ? "DELIVERED" : "PENDING"));
            System.out.println("    Candies: " + h.getOrder().describe());
            System.out.println("    XP reward: " + h.getOrder().getXpReward());
        }
    }

    /**
     * Shows the inventory and lets the player discard candy.
     */
    private void viewInventory() {
        Inventory inventory = player.getInventory();
        inventory.display();
        if (inventory.getUsedSlots() == 0) {
            return;
        }
        System.out.println("1. Discard a candy");
        System.out.println("2. Discard multiple candies");
        System.out.println("3. Back");
        int choice = input.readInt("Choose: ", 1, 3);
        if (choice == 1) {
            int slot = input.readInt("Slot number to discard (1-" + inventory.getUsedSlots() + "): ",
                    1, inventory.getUsedSlots());
            System.out.println("You discarded " + inventory.discard(slot) + ".");
        } else if (choice == 2) {
            List<Integer> slots = input.readIntList(
                    "Slot numbers to discard, separated by spaces (blank to cancel): ");
            List<Candy> removed = inventory.discardMultiple(slots);
            System.out.println("You discarded " + removed.size() + " candies" 
                    + (removed.isEmpty() ? "." : ": " + removed + "."));
        }
    }

    /**
     * Prints the player's information.
     */
    private void printPlayerInfo() {
        Rank next = player.getRank().next();
        System.out.println("===== Player Info =====");
        System.out.println("Player name: " + player.getName());
        System.out.println("Total XP: " + player.getTotalXp());
        System.out.println("XP needed to reach the next rank: "
                + (next == null ? "None (highest rank)" : player.getXpToNextRank()));
        System.out.println("Star rank: " + player.getRank());
        System.out.println("Total steps traveled: " + player.getTotalSteps());
        System.out.println("Day number: " + day);
    }

    /**
     * Asks for confirmation and quits, showing the final stats.
     */
    private void quitGame() {
        if (input.readYesNo("Are you sure you want to quit? (y/n): ")) {
            System.out.println("Thanks for playing! Here are your final stats:");
            printPlayerInfo();
            quit = true;
        }
    }

    /**
     * Congratulates the player and shows the final stats once the game is completed.
     */
    private void endGame() {
        System.out.println();
        System.out.println("*****************************************************");
        System.out.println("Congratulations, " + player.getName() + "! You are a 5-star courier");
        System.out.println("and every order has been delivered. The game is complete!");
        System.out.println("*****************************************************");
        printPlayerInfo();
    }
}