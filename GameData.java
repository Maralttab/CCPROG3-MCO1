import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Holds the starting layout of the world: the home, the 10 factories (from Appendix C of the
 * specifications) and the houses with their orders.
 *
 * <p>NOTE: The factories follow Appendix C. The houses, their coordinates, orders and XP are still
 * PLACEHOLDERS: replace {@link #addHouses(World)} with the values from Appendix D. Houses must sit on
 * tiles at distance 3 + k from home to be unlocked at rank k, and the total XP of the houses unlocked
 * by each rank must be enough to reach the next rank.</p>
 */
public final class GameData {
    private static final CandyType GUMMY = CandyType.GUMMY;
    private static final CandyType LOLLIPOP = CandyType.LOLLIPOP;
    private static final CandyType MARSHMALLOW = CandyType.MARSHMALLOW;
    private static final CandyType PASTILLAS = CandyType.PASTILLAS;
    private static final CandyType YEMA = CandyType.YEMA;
    private static final CandyType POLVORON = CandyType.POLVORON;

    /** The nine flavors that "all flavors" refers to in Appendix C. */
    private static final Flavor[] ALL_FLAVORS = {
        Flavor.UBE, Flavor.MANGO, Flavor.MACAPUNO, Flavor.PANDAN, Flavor.TABLEA,
        Flavor.MELON, Flavor.JACKFRUIT, Flavor.DURIAN, Flavor.GUYABANO};

    /** Prevents instantiation of this utility class. */
    private GameData() {
    }

    /**
     * Fills a world with its home, factories and houses.
     *
     * <p><b>Pre-condition:</b> the world is empty.<br>
     * <b>Post-condition:</b> the home, 10 factories and 29 houses are placed.</p>
     *
     * @param world  the world to fill
     * @param random the random generator the factories use for stocking
     */
    public static void populate(World world, Random random) {
        world.setHome(new PlayerHome());
        addFactories(world, random);
        addHouses(world);
    }

    /**
     * Builds every combination of the given candy types and flavors.
     *
     * @param types   the candy types
     * @param flavors the flavors each type is made in
     * @return the list of candies, type by type
     */
    private static List<Candy> mix(CandyType[] types, Flavor... flavors) {
        List<Candy> result = new ArrayList<>();
        for (CandyType t : types) {
            for (Flavor f : flavors) {
                result.add(new Candy(t, f));
            }
        }
        return result;
    }

    /**
     * Builds the list of candies of one flavored type in several flavors.
     *
     * @param type    the candy type
     * @param flavors the flavors
     * @return the list of candies
     */
    private static List<Candy> mix(CandyType type, Flavor... flavors) {
        return mix(new CandyType[] {type}, flavors);
    }

    /**
     * Joins several lists of candies into one.
     *
     * @param lists the lists to join
     * @return the combined list
     */
    @SafeVarargs
    private static List<Candy> join(List<Candy>... lists) {
        List<Candy> result = new ArrayList<>();
        for (List<Candy> l : lists) {
            result.addAll(l);
        }
        return result;
    }

    /**
     * Adds a factory using the final (15x15) row and column given in Appendix C.
     *
     * @param w        the world
     * @param r        the random generator the factory uses for stocking
     * @param number   the factory number
     * @param row      the row in the final 15x15 world (0 is the top row)
     * @param column   the column in the final 15x15 world (0 is the leftmost column)
     * @param produced the candies the factory can generate
     */
    private static void factory(World w, Random r, int number, int row, int column, List<Candy> produced) {
        w.addFactory(new Factory("Factory " + number, column - World.MAX_RADIUS, World.MAX_RADIUS - row, r, produced));
    }

    /**
         * Adds a house using its name, final (15x15) row and column, and order from Appendix D.
         *
         * @param w      the world
         * @param name   the name of the house (e.g. "House A")
         * @param row    the row in the final 15x15 world (0 is the top row)
         * @param column the column in the final 15x15 world (0 is the leftmost column)
         * @param order  the order required by the house
         */
        private static void house(World w, String name, int row, int column, Order order) {
            w.addHouse(new House(name, column - World.MAX_RADIUS, World.MAX_RADIUS - row, order));
        }

    /**
     * Places the 10 factories of Appendix C. Each one appears when its tile is unlocked.
     *
     * @param w the world
     * @param r the random generator used for stocking
     */
    private static void addFactories(World w, Random r) {
        Flavor[] ubeToPandan = {Flavor.UBE, Flavor.MANGO, Flavor.MACAPUNO, Flavor.PANDAN};
        Flavor[] withCorn = {Flavor.UBE, Flavor.MANGO, Flavor.MACAPUNO, Flavor.PANDAN, Flavor.CANDY_CORN};
        Flavor[] tableaMelon = {Flavor.TABLEA, Flavor.MELON};
        Flavor[] tropical = {Flavor.JACKFRUIT, Flavor.DURIAN, Flavor.GUYABANO};

        // Factory 1 (introduced at 0-star): Candy Corn and Lollipop (Ube, Mango, Macapuno, Pandan)
        factory(w, r, 1, 4, 10, join(List.of(Candy.plain(CandyType.CANDY_CORN)), mix(LOLLIPOP, ubeToPandan)));
        // Factory 2 (0-star): Gummy and Marshmallow (Ube, Mango, Macapuno, Pandan, Candy Corn)
        factory(w, r, 2, 10, 4, mix(new CandyType[] {GUMMY, MARSHMALLOW}, withCorn));
        // Factory 3 (1-star): Pastillas and Yema (with Candy Corn flavor), Polvoron (Ube to Pandan)
        factory(w, r, 3, 3, 3, join(mix(new CandyType[] {PASTILLAS, YEMA}, withCorn), mix(POLVORON, ubeToPandan)));
        // Factory 4 (2-star): Gummy, Lollipop, Marshmallow, Pastillas, Yema, Polvoron in Tablea and Melon
        factory(w, r, 4, 12, 12, mix(new CandyType[] {GUMMY, LOLLIPOP, MARSHMALLOW, PASTILLAS, YEMA, POLVORON}, tableaMelon));
        // Factory 5 (3-star): Gummy, Lollipop, Marshmallow in Jackfruit, Durian and Guyabano
        factory(w, r, 5, 1, 11, mix(new CandyType[] {GUMMY, LOLLIPOP, MARSHMALLOW}, tropical));
        // Factory 6 (3-star): Lollipop in all flavors
        factory(w, r, 6, 13, 7, mix(LOLLIPOP, ALL_FLAVORS));
        // Factory 7 (3-star): Gummy in all flavors
        factory(w, r, 7, 6, 13, mix(GUMMY, ALL_FLAVORS));
        // Factory 8 (3-star): Marshmallow in all flavors
        factory(w, r, 8, 7, 1, mix(MARSHMALLOW, ALL_FLAVORS));
        // Factory 9 (4-star): Candy Corn, Candy Cane, and Pastillas/Yema/Polvoron in Ube, Mango and Melon
        factory(w, r, 9, 0, 0, join(List.of(Candy.plain(CandyType.CANDY_CORN), Candy.plain(CandyType.CANDY_CANE)),
                mix(new CandyType[] {PASTILLAS, YEMA, POLVORON}, Flavor.UBE, Flavor.MANGO, Flavor.MELON)));
        // Factory 10 (4-star): Pastillas, Yema, Polvoron in Macapuno, Pandan, Jackfruit, Durian, Guyabano
        factory(w, r, 10, 14, 2, mix(new CandyType[] {PASTILLAS, YEMA, POLVORON},
                Flavor.MACAPUNO, Flavor.PANDAN, Flavor.JACKFRUIT, Flavor.DURIAN, Flavor.GUYABANO));
    }

    /**
     * Places the PLACEHOLDER houses, grouped by the rank that unlocks them. Each order only asks for
     * candies that the factories accessible at that rank can generate.
     *
     * @param w the world
     *
    private static void addHouses(World w) {
        int[][][] positions = {
            {{0, 2}, {3, 0}, {0, -3}, {-3, 1}},
            {{4, 2}, {-4, -1}, {1, 4}, {-2, -4}, {4, -3}},
            {{5, 3}, {-5, 2}, {2, 5}, {-4, 5}, {3, -5}, {-5, -4}},
            {{6, 2}, {-6, 3}, {0, 6}, {-3, -6}, {5, -6}, {6, -5}},
            {{7, 0}, {7, 6}, {-7, -2}, {-7, 5}, {2, 7}, {-5, 7}, {1, -7}, {-6, -7}}};
        int[] xp = {40, 90, 180, 300, 300};
        int[] kinds = {2, 3, 3, 4, 4};
        int[] quantity = {3, 3, 4, 5, 6};

        int number = 0;
        for (int ring = 0; ring < positions.length; ring++) {
            List<Candy> pool = candiesAvailableAt(w, ring);
            for (int j = 0; j < positions[ring].length; j++) {
                Order order = new Order(xp[ring]);
                for (int t = 0; t < kinds[ring]; t++) {
                    order.addItem(pool.get((j * 7 + t * 5 + ring * 3) % pool.size()), quantity[ring]);
                }
                String label = number < 26 ? String.valueOf((char) ('A' + number))
                        : "A" + (char) ('A' + number - 26);
                w.addHouse(new House("House " + label, positions[ring][j][0], positions[ring][j][1], order));
                number++;
            }
        }
    }*/

    /**
     * Places Houses A–P using their exact coordinates, candy orders, and XP rewards from Appendix D.
     *
     * @param w the world to add houses to
     */
    private static void addHouses(World w) {
        // 0 Star Houses
        // House A
        Order orderA = new Order(100);
        orderA.addItem(Candy.plain(CandyType.CANDY_CORN), 4);
        orderA.addItem(new Candy(GUMMY, Flavor.UBE), 2);
        orderA.addItem(new Candy(LOLLIPOP, Flavor.UBE), 2);
        orderA.addItem(new Candy(MARSHMALLOW, Flavor.UBE), 2);
        house(w, "House A", 5, 5, orderA);

        // House B
        Order orderB = new Order(150);
        orderB.addItem(Candy.plain(CandyType.CANDY_CORN), 15);
        house(w, "House B", 9, 6, orderB);

        // House C
        Order orderC = new Order(250);
        orderC.addItem(new Candy(GUMMY, Flavor.UBE), 5);
        orderC.addItem(new Candy(MARSHMALLOW, Flavor.UBE), 5);
        orderC.addItem(new Candy(PASTILLAS, Flavor.UBE), 5);
        orderC.addItem(new Candy(YEMA, Flavor.UBE), 5);
        orderC.addItem(new Candy(POLVORON, Flavor.UBE), 5);
        house(w, "House C", 7, 10, orderC);

        // 1 Star Houses
        // House D
        Order orderD = new Order(400);
        orderD.addItem(Candy.plain(CandyType.CANDY_CORN), 5);
        orderD.addItem(Candy.plain(CandyType.CANDY_CANE), 5);
        orderD.addItem(new Candy(PASTILLAS, Flavor.UBE), 10);
        orderD.addItem(new Candy(YEMA, Flavor.UBE), 10);
        orderD.addItem(new Candy(POLVORON, Flavor.UBE), 10);
        house(w, "House D", 11, 9, orderD);

        // 2 Star Houses
        // House E
        Order orderE = new Order(300);
        orderE.addItem(Candy.plain(CandyType.CANDY_CORN), 10);
        orderE.addItem(new Candy(YEMA, Flavor.UBE), 5);
        orderE.addItem(new Candy(PASTILLAS, Flavor.UBE), 5);
        orderE.addItem(new Candy(LOLLIPOP, Flavor.UBE), 5);
        orderE.addItem(new Candy(LOLLIPOP, Flavor.MACAPUNO), 5);
        house(w, "House E", 2, 8, orderE);

        // House F
        Order orderF = new Order(300);
        orderF.addItem(new Candy(GUMMY, Flavor.TABLEA), 7);
        orderF.addItem(new Candy(MARSHMALLOW, Flavor.TABLEA), 7);
        orderF.addItem(new Candy(PASTILLAS, Flavor.TABLEA), 8);
        orderF.addItem(new Candy(POLVORON, Flavor.TABLEA), 8);
        house(w, "House F", 12, 3, orderF);

        // House G
        Order orderG = new Order(400);
        orderG.addItem(new Candy(PASTILLAS, Flavor.UBE), 20);
        orderG.addItem(new Candy(PASTILLAS, Flavor.DURIAN), 10);
        orderG.addItem(new Candy(PASTILLAS, Flavor.JACKFRUIT), 10);
        house(w, "House G", 6, 2, orderG);

        // House H
        Order orderH = new Order(300);
        orderH.addItem(new Candy(POLVORON, Flavor.UBE), 10);
        orderH.addItem(new Candy(YEMA, Flavor.UBE), 5);
        orderH.addItem(new Candy(PASTILLAS, Flavor.UBE), 5);
        orderH.addItem(new Candy(GUMMY, Flavor.MANGO), 5);
        orderH.addItem(new Candy(MARSHMALLOW, Flavor.PANDAN), 5);
        house(w, "House H", 8, 12, orderH);

        // 3 Star Houses
        // House I
        Order orderI = new Order(450);
        orderI.addItem(new Candy(LOLLIPOP, Flavor.JACKFRUIT), 9);
        orderI.addItem(new Candy(LOLLIPOP, Flavor.DURIAN), 9);
        orderI.addItem(new Candy(LOLLIPOP, Flavor.PANDAN), 9);
        orderI.addItem(new Candy(LOLLIPOP, Flavor.UBE), 9);
        orderI.addItem(new Candy(LOLLIPOP, Flavor.MACAPUNO), 9);
        house(w, "House I", 1, 4, orderI);

        // House J
        Order orderJ = new Order(350);
        orderJ.addItem(Candy.plain(CandyType.CANDY_CORN), 5);
        orderJ.addItem(new Candy(MARSHMALLOW, Flavor.MANGO), 6);
        orderJ.addItem(new Candy(YEMA, Flavor.MANGO), 7);
        orderJ.addItem(new Candy(PASTILLAS, Flavor.MANGO), 8);
        orderJ.addItem(new Candy(GUMMY, Flavor.MANGO), 9);
        house(w, "House J", 4, 1, orderJ);

        // House K
        Order orderK = new Order(400);
        orderK.addItem(new Candy(GUMMY, Flavor.UBE), 10);
        orderK.addItem(new Candy(GUMMY, Flavor.MANGO), 10);
        orderK.addItem(new Candy(GUMMY, Flavor.MACAPUNO), 10);
        orderK.addItem(new Candy(GUMMY, Flavor.PANDAN), 10);
        house(w, "House K", 10, 1, orderK);

        // House L
        Order orderL = new Order(300);
        orderL.addItem(new Candy(PASTILLAS, Flavor.UBE), 10);
        orderL.addItem(new Candy(YEMA, Flavor.UBE), 10);
        orderL.addItem(new Candy(POLVORON, Flavor.UBE), 10);
        house(w, "House L", 13, 6, orderL);

        // House M
        Order orderM = new Order(300);
        orderM.addItem(new Candy(PASTILLAS, Flavor.PANDAN), 10);
        orderM.addItem(new Candy(YEMA, Flavor.PANDAN), 10);
        orderM.addItem(new Candy(POLVORON, Flavor.PANDAN), 10);
        house(w, "House M", 13, 13, orderM);

        // 4 Star Houses
        // House N
        Order orderN = new Order(250);
        orderN.addItem(Candy.plain(CandyType.CANDY_CANE), 16);
        orderN.addItem(new Candy(POLVORON, Flavor.JACKFRUIT), 3);
        orderN.addItem(new Candy(POLVORON, Flavor.DURIAN), 3);
        orderN.addItem(new Candy(LOLLIPOP, Flavor.TABLEA), 3);
        house(w, "House N", 3, 14, orderN);

        // House O
        Order orderO = new Order(1000);
        orderO.addItem(Candy.plain(CandyType.CANDY_CANE), 100);
        house(w, "House O", 14, 0, orderO);

        // House P
        Order orderP = new Order(250);
        orderP.addItem(Candy.plain(CandyType.CANDY_CORN), 5);
        orderP.addItem(Candy.plain(CandyType.CANDY_CANE), 5);
        orderP.addItem(new Candy(PASTILLAS, Flavor.MACAPUNO), 5);
        orderP.addItem(new Candy(YEMA, Flavor.MACAPUNO), 5);
        orderP.addItem(new Candy(POLVORON, Flavor.MACAPUNO), 5);
        house(w, "House P", 14, 10, orderP);
    }

    /**
     * Lists the candies the factories accessible at a rank can generate.
     *
     * @param w    the world holding the factories
     * @param rank the rank (in stars)
     * @return the distinct candies, in factory order
     */
    private static List<Candy> candiesAvailableAt(World w, int rank) {
        Set<Candy> candies = new LinkedHashSet<>();
        for (Factory f : w.getFactories()) {
            if (f.getUnlockStars() <= rank) {
                candies.addAll(f.getProducedCandies());
            }
        }
        return new ArrayList<>(candies);
    }
}
