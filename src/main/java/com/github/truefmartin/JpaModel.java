package com.github.truefmartin;

import com.github.truefmartin.exceptions.EmptyResultsException;
import com.github.truefmartin.models.*;
import org.hibernate.Session;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The JpaModel class is responsible for managing the database operations.
 * It uses Hibernate to interact with the database.
 */
public class JpaModel implements AutoCloseable{

    /**
     * Constructs a new JpaModel object.
     * If the sessionFactory is null, it builds a new one.
     */
    public JpaModel() {
        if (!HibernateUtil.isOpen()) {
            HibernateUtil.start();
        }
    }

    /**
     * Closes the HibernateUtil sessionFactory when the JpaModel object is closed.
     * @throws Exception if there is a problem closing the sessionFactory
     */
    @Override
    public void close() throws Exception {
        HibernateUtil.close();
    }

    /*
     * The following methods are used to interact with the database. Each method opens a new session,
     * performs the database operation, and then closes the session even if an exception is thrown.
     */

    /**
     * Retrieves the menus of a specific restaurant.
     * @param restaurantName the name of the restaurant
     * @param cityName the city where the restaurant is located
     * @return a list of menus
     * @throws EmptyResultsException if no menus are found
     */
//    public static List<DisplayDishMenu> getMenusOfRestaurant(String restaurantName, String cityName) throws EmptyResultsException {
//        try(var tx = HibernateUtil.openSession()) {
//            return getMenusOfRestaurant(tx, restaurantName, cityName);
//        }
//    }

    /**
     * Retrieves the menus that contain a specific dish.
     * @param dishName the name of the dish
     * @return a list of menus
     * @throws EmptyResultsException if no menus are found
     */
//    public static List<DisplayRestaurantMenu> getMenusOfDish(String dishName) throws EmptyResultsException {
//        try(var tx = HibernateUtil.openSession()) {
//            return getMenusOfDish(tx, dishName);
//        }
//    }

    /**
     * Retrieves the orders of a specific restaurant.
     * @param restaurantName the name of the restaurant
     * @param cityName the city where the restaurant is located
     * @return a list of orders
     * @throws EmptyResultsException if no orders are found
     */
//    public static List<DisplayDishMenuOrder> getOrdersOfRestaurant(String restaurantName, String cityName) throws EmptyResultsException {
//        try(var tx = HibernateUtil.openSession()) {
//            return getOrdersOfRestaurant(tx, restaurantName, cityName);
//        }
//    }

    public static void addGame(Game game) {
        try(var tx = HibernateUtil.openSession()) {
            addGame(tx, game);
        }
    }

    /**
     * Retrieves all orders from the database.
     * @return a list of all orders
     * @throws EmptyResultsException if no orders are found
     */
//    public static List<DisplayRestaurantDishOrder> getAllOrders() throws EmptyResultsException {
//        try(var tx = HibernateUtil.openSession()) {
//            return getAllOrders(tx);
//        }
//    }

    /**
     * Deletes an order from the database.
     * @param order the order to be deleted
     */
//    public static void deleteOrder(FoodOrderEntity order) {
//        try(var tx = HibernateUtil.openSession()) {
//            deleteOrder(tx, order);
//        }
//    }

    public static void addTeam(Team team) {
        try(var tx = HibernateUtil.openSession()) {
            addTeam(tx, team);
        }
    }

    private static void addTeam(Session tx, Team team) {
        tx.beginTransaction();
        tx.persist(team);
        tx.getTransaction().commit();
    }

    public static void addPlayer(Player player) {
        try(var tx = HibernateUtil.openSession()) {
            addPlayer(tx, player);
        }
    }

    private static void addPlayer(Session tx, Player player) {
        tx.beginTransaction();
        tx.persist(player);
        tx.getTransaction().commit();
    }


    public static List<Player> getPlayersOfTeam(Team team) {
        try(var tx = HibernateUtil.openSession()) {
            return getPlayersOfTeam(tx, team);
        }
    }

    private static List<Player> getPlayersOfTeam(Session tx, Team team) {
        return tx.createQuery(
                "from Player p where p.team = :team",
                Player.class
        ).setParameter("team", team).getResultList();
    }

    public static List<Player> getPlayerOfPosition(Player.PositionType position) {
        try(var tx = HibernateUtil.openSession()) {
            return getPlayerOfPosition(tx, position);
        }
    }

    private static List<Player> getPlayerOfPosition(Session tx, Player.PositionType position) {
        return tx.createQuery(
                "from Player p where p.position = :position",
                Player.class
        ).setParameter("position", position).getResultList();
    }

    public static LinkedHashMap<Team, Integer[]> getTeamsAndWins() {
        try(var tx = HibernateUtil.openSession()) {
            var games = getTeamsAndGames(tx);
            //  View all teams arranged by conference (sorted alphabetically); within each conference, sort by number of wins overall and then number of wins within the same conference
            LinkedHashMap<Team, Integer[]> teamScoresMap = new LinkedHashMap<>();
            for (Game game :
                    games
            ) {
                Team homeTeam = game.getHomeTeam();
                Team awayTeam = game.getAwayTeam();
                if (!teamScoresMap.containsKey(homeTeam)) {
                    teamScoresMap.put(homeTeam, new Integer[]{0, 0});
                }
                if (!teamScoresMap.containsKey(awayTeam)) {
                    teamScoresMap.put(awayTeam, new Integer[]{0, 0});
                }
                if (Objects.equals(game.getScore1(), game.getScore2())) {
                    continue;
                }
                boolean conferenceIsSame = homeTeam.getConference().equals(awayTeam.getConference());
                // We want to add the team to the map even if they have not yet won a game, but don't want to increment on tie
                Team winningTeam = game.getScore1() > game.getScore2() ? homeTeam : awayTeam;
                var scoreArr = teamScoresMap.get(winningTeam);
                scoreArr[0]++;
                if (conferenceIsSame) {
                    scoreArr[1]++;
                }
            }
            return teamScoresMap.entrySet().stream()
                    .sorted((e1, e2) -> {
                        // First compare on conference name spelling
                        int confCompare = e1.getKey().getConference().toString().compareTo(e2.getKey().getConference().toString());
                        if (confCompare != 0) {
                            return confCompare;
                        }
                        // If same conference, compare on total wins
                        int winsCompare = e2.getValue()[0].compareTo(e1.getValue()[0]);
                        if (winsCompare != 0) {
                            return winsCompare;
                        }
                        return e2.getValue()[1].compareTo(e1.getValue()[1]);
                    })
                    .collect(LinkedHashMap::new, (LinkedHashMap<Team, Integer[]> map, Map.Entry<Team, Integer[]> entry) -> map.put(entry.getKey(), entry.getValue()), Map::putAll);
        }
    }

    private static List<Game> getTeamsAndGames(Session tx) {
        var games =  tx.createQuery(
                "from Game g ",
                Game.class
        ).getResultList();
        var count = games.size();
        return games;
    }

    /*
    * The following methods are helper methods that perform the actual database operations.
    */

//    private static List<DisplayRestaurantMenu> getMenusOfDish(Session tx, String dishName) throws EmptyResultsException {
//        List<MenuItemEntity> menus = tx.createQuery(
//                        "select d.menuItems " +
//                                "from DishEntity d " +
//                                "where d.dishName = :dishName ",
//                        MenuItemEntity.class
//                )
//                .setParameter("dishName", dishName)
//                .getResultList();
//        if (menus.isEmpty()) {
//            throw EmptyResultsException.fromInput(dishName, " or no 'menu_items' with that dishNo");
//        }
//        var result = new ArrayList<DisplayRestaurantMenu>();
//        for (MenuItemEntity menu :
//                menus
//        ) {
//            result.add(new DisplayRestaurantMenu(menu.getRestaurant(), menu));
//        }
//        return result;
//    }
//
//    private static List<DisplayDishMenu> getMenusOfRestaurant(Session tx, String restaurantName, String cityName) throws EmptyResultsException {
//
//        List<MenuItemEntity> menus = tx.createQuery(
//                        "select elements(r.menuItems) " +
//                                "from RestaurantEntity r " +
//                                "where r.restaurantName = :rName " +
//                                "and r.city = :rCity",
//                        MenuItemEntity.class
//                )
//                .setParameter("rName", restaurantName)
//                .setParameter("rCity", cityName)
//                .getResultList();
//
//        if (menus.isEmpty()) {
//            throw EmptyResultsException.fromInput(restaurantName, cityName);
//        }
//        List<DisplayDishMenu> result = new ArrayList<>();
//        for (MenuItemEntity dishMenu :
//                menus
//        ) {
//            if (dishMenu.getDish() != null) {
//                result.add(new DisplayDishMenu(dishMenu.getDish(), dishMenu));
//            } else {
//                result.add(new DisplayDishMenu(String.format("**Menu item_no=%d, with price %.2f has no associated dish**\n",
//                        dishMenu.getItemNo(), dishMenu.getPrice())));
//            }
//        }
//        return result;
//    }


    // Add the itemNo, current time, and current date to the FoodOrder table.
    private static void addGame(Session tx, Game game) {
        tx.beginTransaction();
        // Get menu from cache to avoid LazyInitializationException
//        menu = tx.get(MenuItemEntity.class, menu.getItemNo());
//        FoodOrderEntity newOrder = new FoodOrderEntity();
//        newOrder.setMenu(menu);
//        newOrder.setDateTimeNow();
        tx.persist(game);
//        menu.getFoodOrders().add(newOrder);
        tx.getTransaction().commit();
    }

//    private static List<DisplayDishMenuOrder> getOrdersOfRestaurant(Session tx, String restaurantName, String cityName) throws EmptyResultsException {
//        // The number of queries gets out of hand if we let the EAGER associations do their own thing,
//        // so we will instead do a single query with raw sql instead. Trading readability and persistence for less DB strain.
//        List<Object[]> results = tx.createNativeQuery(
//                        "SELECT dish_name, price, date, time " +
//                                "FROM food_order o " +
//                                "JOIN menu_item mi on o.item_no = mi.item_no " +
//                                "JOIN dish d on mi.dish_no = d.dish_no " +
//                                "WHERE mi.restaurant_no in " +
//                                "( " +
//                                "SELECT restaurant.restaurant_id " +
//                                "FROM true.restaurant " +
//                                "WHERE restaurant_name = :rName " +
//                                "AND city = :rCity" +
//                                ")",
//                        Object[].class
//                )
//                .setParameter("rName", restaurantName)
//                .setParameter("rCity", cityName)
//                .list();
//
//        if (results.isEmpty()) {
//            throw EmptyResultsException.fromInput(restaurantName, cityName, " with possibly no menus for given restaurant");
//        }
//        List<DisplayDishMenuOrder> displayOrders = new ArrayList<>();
//        for (Object[] result :
//                results
//        ) {
//            displayOrders.add(new DisplayDishMenuOrder(result));
//        }
//        return displayOrders;
//    }
//
//    private static List<DisplayRestaurantDishOrder> getAllOrders(Session tx) throws EmptyResultsException {
//        List<DisplayRestaurantDishOrder> restaurantDishOrders = tx.createQuery(
//                        "select new com.github.truefmartin.views.DisplayRestaurantDishOrder(o.menu.restaurant, o.menu.dish, o)" +
//                                "from FoodOrderEntity o ",
//                        DisplayRestaurantDishOrder.class
//                )
//                .getResultList();
//        if (restaurantDishOrders.isEmpty()) {
//            throw new EmptyResultsException("found no orders in food_order");
//        }
//        return restaurantDishOrders;
//    }
//
//    private static void deleteOrder(Session tx, FoodOrderEntity order) {
//        tx.beginTransaction();
//        tx.remove(order);
//        tx.getTransaction().commit();
//    }
//
//    private static RestaurantEntity getRestaurant(Session tx, String restaurantName, String cityName) throws EmptyResultsException {
//        var restaurant = tx.createQuery(
//                        "from RestaurantEntity r " +
//                                "where r.restaurantName = :rName " +
//                                "and r.city = :rCity",
//                        RestaurantEntity.class
//                )
//                .setParameter("rName", restaurantName)
//                .setParameter("rCity", cityName).getSingleResultOrNull();
//        if (restaurant == null ) {
//            throw EmptyResultsException.fromInput(restaurantName, cityName);
//        }
//        return restaurant;
//    }
//
//    private static void addDish(Session tx, DishEntity dish) {
//        tx.beginTransaction();
//        tx.persist(dish);
//        tx.getTransaction().commit();
//    }

    public List<String> listRelation(String relationName, Class<?> className) {
        try(var tx = HibernateUtil.openSession()) {
            return listRelation(tx, relationName, className).stream().map(Object::toString).collect(Collectors.toList());
        }
    }

    private List<?> listRelation(Session tx, String relationName, Class<?> className) {
        var entityName = "";
        switch (relationName){
            case "game":
                entityName = "Game";
                break;
            case "team":
                entityName = "Team";
                break;
            case "player":
                entityName = "Player";
                break;
        }
        return tx.createQuery(
                "FROM " + entityName + " e",
                className
                ).getResultList();
    }
}
