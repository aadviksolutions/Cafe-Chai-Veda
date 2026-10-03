package com.aadvik.chaivedapos.data

import android.content.ContentValues
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * CHAI VEDA master menu.
 * This file contains ONLY the menu data. It does not modify billing,
 * reports, settings, theme, inventory, tables, printer or other modules.
 */
object MenuSeed {

    private data class Item(
        val name: String,
        val category: String,
        val price: Double,
        val foodType: String = "Veg"
    )

    private val items = listOf(
        // BEVERAGES - Tea Collection
        Item("Regular Tea", "Tea Collection", 30.0),
        Item("Ginger Tea", "Tea Collection", 40.0),
        Item("Green Tea", "Tea Collection", 40.0),
        Item("Elaichi Tea", "Tea Collection", 45.0),
        Item("Jaggery Tea", "Tea Collection", 50.0),
        Item("Honey Lemon Tea", "Tea Collection", 60.0),

        // BEVERAGES - Hot Coffee Selection
        Item("Regular Coffee", "Hot Coffee Selection", 50.0),
        Item("Black Coffee", "Hot Coffee Selection", 50.0),
        Item("Hazelnut Coffee", "Hot Coffee Selection", 70.0),
        Item("Caramel Coffee", "Hot Coffee Selection", 80.0),
        Item("Toffee Coffee", "Hot Coffee Selection", 80.0),
        Item("Café Mocha", "Hot Coffee Selection", 90.0),

        // BEVERAGES - Iced Coffee & Shakes
        Item("Cold Coffee", "Iced Coffee & Shakes", 130.0),
        Item("Cold Coffee (with Ice-Cream)", "Iced Coffee & Shakes", 150.0),
        Item("Chocolate Cold Coffee", "Iced Coffee & Shakes", 170.0),

        // BEVERAGES - Juices
        Item("Watermelon Juice", "Juices", 70.0),
        Item("Orange Juice", "Juices", 70.0),
        Item("Pineapple Juice", "Juices", 80.0),
        Item("Apple Juice", "Juices", 80.0),

        // BEVERAGES - Salad
        Item("Healthy Salad", "Salad", 140.0),
        Item("CCV Special", "Salad", 150.0),

        // BEVERAGES - Premium Milkshakes
        Item("Strawberry Shake", "Premium Milkshakes", 150.0),
        Item("Mango Shake", "Premium Milkshakes", 150.0),
        Item("KitKat Shake", "Premium Milkshakes", 160.0),
        Item("Oreo Shake", "Premium Milkshakes", 160.0),
        Item("Biscoff Shake", "Premium Milkshakes", 160.0),
        Item("Brownie Shake", "Premium Milkshakes", 200.0),

        // BEVERAGES - Refreshing Mojitos
        Item("Virgin Mojito", "Refreshing Mojitos", 110.0),
        Item("Blue Curacao Mojito", "Refreshing Mojitos", 120.0),
        Item("Black Currant Mojito", "Refreshing Mojitos", 120.0),
        Item("Bubble Gum Mojito", "Refreshing Mojitos", 120.0),
        Item("Cranberry Mojito", "Refreshing Mojitos", 120.0),
        Item("CCV Special Mojito", "Refreshing Mojitos", 130.0),

        // SNACK ATTACK - Butter & Toasts
        Item("Butter Toast", "Butter & Toasts", 70.0),
        Item("Caramel Butter Toast", "Butter & Toasts", 80.0),
        Item("Bun Maska", "Butter & Toasts", 90.0),
        Item("Nutella Toast", "Butter & Toasts", 99.0),

        // SNACK ATTACK - Burgers
        Item("Aloo Tikki Burger", "Burgers", 90.0),
        Item("Veg Burger", "Burgers", 120.0),
        Item("Surprise Burger", "Burgers", 130.0),
        Item("Paneer Tikki Burger", "Burgers", 140.0),
        Item("Chai Veda Special Burger", "Burgers", 150.0),

        // SNACK ATTACK - Sandwiches & Breads
        Item("Vegetable Grill Sandwich", "Sandwiches & Breads", 100.0),
        Item("Club Sandwich", "Sandwiches & Breads", 120.0),
        Item("Grilled Cheese Sandwich", "Sandwiches & Breads", 120.0),
        Item("Cheese Corn Sandwich", "Sandwiches & Breads", 130.0),
        Item("Bombay Sandwich", "Sandwiches & Breads", 130.0),
        Item("Garlic Bread", "Sandwiches & Breads", 140.0),
        Item("Mexican Sandwich", "Sandwiches & Breads", 150.0),
        Item("Paneer Tikka Sandwich", "Sandwiches & Breads", 160.0),
        Item("Cheese Garlic Bread", "Sandwiches & Breads", 180.0),

        // SNACK ATTACK - Fries & Nachos
        Item("Salted Fries", "Fries & Nachos", 80.0),
        Item("Chaat Masala Fries", "Fries & Nachos", 90.0),
        Item("Peri Peri Fries", "Fries & Nachos", 120.0),
        Item("Cheese Fries", "Fries & Nachos", 140.0),
        Item("Nachos with Salsa", "Fries & Nachos", 140.0),
        Item("Loaded Nachos", "Fries & Nachos", 180.0),

        // SNACK ATTACK - Paratha
        Item("Aloo Paratha", "Paratha", 90.0),
        Item("Paneer Paratha", "Paratha", 130.0),
        Item("Dal Khichdi", "Paratha", 160.0),

        // SNACK ATTACK - All Time Fav.
        Item("Sabundaba Vada", "All Time Fav.", 95.0),
        Item("Sabundaba Khichdi", "All Time Fav.", 140.0),

        // SNACK ATTACK - Wraps
        Item("Aloo Crispy Wrap", "Wraps", 140.0),
        Item("Veg Crispy Wrap", "Wraps", 150.0),
        Item("Paneer Lajawab Wrap", "Wraps", 170.0),

        // SNACK ATTACK - Vada Pav
        Item("Bombay Vada Pav", "Vada Pav", 50.0),
        Item("Schezwan Vada Pav", "Vada Pav", 70.0),
        Item("Cheese Vada Pav", "Vada Pav", 85.0),
        Item("Paneer Vada Pav", "Vada Pav", 110.0),

        // KITCHEN SPECIALS - Maggie & Noodles
        Item("Plain Maggi", "Maggie & Noodles", 100.0),
        Item("Veg Maggi", "Maggie & Noodles", 130.0),
        Item("Schezwan Maggi", "Maggie & Noodles", 120.0),
        Item("Hakka Noodles", "Maggie & Noodles", 140.0),
        Item("Cheese Corn Maggi", "Maggie & Noodles", 160.0),
        Item("Chai Veda Special Maggi", "Maggie & Noodles", 160.0),
        Item("Schezwan Noodles", "Maggie & Noodles", 160.0),
        Item("Chilli Garlic Noodles", "Maggie & Noodles", 180.0),
        Item("Paneer Schez. Noodles", "Maggie & Noodles", 200.0),

        // KITCHEN SPECIALS - Pasta
        Item("Red Sauce Pasta", "Pasta", 140.0),
        Item("White Sauce Pasta", "Pasta", 160.0),
        Item("Pink Sauce Pasta", "Pasta", 180.0),

        // KITCHEN SPECIALS - Pizza
        Item("Margherita Pizza", "Pizza", 200.0),
        Item("Onion Capsicum Pizza", "Pizza", 210.0),
        Item("Corn Delight Pizza", "Pizza", 220.0),
        Item("Mexican Pizza", "Pizza", 240.0),
        Item("Tandoori Paneer Pizza", "Pizza", 260.0),
        Item("Farmhouse Pizza", "Pizza", 280.0),

        // KITCHEN SPECIALS - Chinese Starters
        Item("Crispy Corn", "Chinese Starters", 150.0),
        Item("Chana Roast", "Chinese Starters", 150.0),
        Item("Honey Chilli Potato", "Chinese Starters", 160.0),
        Item("Manchurian Dry", "Chinese Starters", 160.0),
        Item("Manchurian Gravy", "Chinese Starters", 180.0),
        Item("Paneer 65", "Chinese Starters", 180.0),
        Item("Paneer Chilli", "Chinese Starters", 190.0),

        // KITCHEN SPECIALS - Fried Rice
        Item("Veg Fried Rice", "Fried Rice", 150.0),
        Item("Lemon Rice", "Fried Rice", 160.0),
        Item("Schezwan Fried Rice", "Fried Rice", 170.0),
        Item("Manchurian Fried Rice", "Fried Rice", 180.0),
        Item("Paneer Fried Rice", "Fried Rice", 200.0),
        Item("Triple Fried Rice", "Fried Rice", 210.0),

        // KITCHEN SPECIALS - Soups
        Item("Lemon Coriander Soup", "Soups", 90.0),
        Item("Manchow Soup", "Soups", 120.0),
        Item("Hot & Sour Soup", "Soups", 120.0),

        // KITCHEN SPECIALS - Desserts
        Item("Vanilla Ice Cream", "Desserts", 50.0),
        Item("Mango Ice Cream", "Desserts", 60.0),
        Item("Chocolate Ice Cream", "Desserts", 80.0),
        Item("Choco Lava", "Desserts", 90.0),
        Item("Brownie", "Desserts", 90.0),
        Item("CCV Special Dessert", "Desserts", 120.0)
    )

    fun itemCount(): Int = items.size

    /** Replace all existing menu rows with the master menu. */
    fun replaceMenu(database: SupportSQLiteDatabase) {
        database.beginTransaction()
        try {
            database.delete("menu_products", null, null)

            items.forEach { item ->
                val values = ContentValues().apply {
                    put("name", item.name)
                    put("category", item.category)
                    put("price", item.price)
                    put("foodType", item.foodType)
                    put("available", 1)
                    put("imageUri", "")
                }
                database.insert("menu_products", 0, values)
            }

            database.setTransactionSuccessful()
        } finally {
            database.endTransaction()
        }
    }
}
