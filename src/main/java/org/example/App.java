package org.example;

import org.example.model.Item;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.*;

public class App {

    public static void main(String[] args) {
        Locale locale = selectLocale();
        ResourceBundle bundle = getBundle(locale);
        Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);

        int numItems = readNonNegativeInt(scanner, bundle.getString("prompt.numItems"), bundle);

        List<Item> items = new ArrayList<>();
        for (int i = 1; i <= numItems; i++) {
            String pricePrompt = MessageFormat.format(bundle.getString("prompt.itemPrice"), i);
            double price = readNonNegativeDouble(scanner, pricePrompt, bundle);

            String qtyPrompt = MessageFormat.format(bundle.getString("prompt.itemQty"), i);
            int qty = readNonNegativeInt(scanner, qtyPrompt, bundle);

            Item item = new Item(price, qty);
            items.add(item);

            double itemTotal = ShoppingCartCalculator.calculateItemTotal(item);
            NumberFormat nf = NumberFormat.getNumberInstance(locale);
            nf.setMinimumFractionDigits(2);
            nf.setMaximumFractionDigits(2);
            String itemTotalMsg = MessageFormat.format(bundle.getString("output.total"), nf.format(itemTotal));
            System.out.println(itemTotalMsg);
        }

        double cartTotal = ShoppingCartCalculator.calculateCartTotal(items);
        NumberFormat nf = NumberFormat.getNumberInstance(locale);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        System.out.println(MessageFormat.format(bundle.getString("output.total"), nf.format(cartTotal)));
    }

    private static Locale selectLocale() {
        Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
        System.out.println("Select language / Valitse kieli / Välj språk / 言語を選択:");
        System.out.println("1) English  2) Suomi  3) Svenska  4) 日本語");
        int choice = 1;
        try {
            String line = scanner.nextLine();
            choice = Integer.parseInt(line.trim());
        } catch (Exception ignored) {
        }

        return switch (choice) {
            case 2 -> new Locale("fi", "FI");
            case 3 -> new Locale("sv", "SE");
            case 4 -> new Locale("ja", "JP");
            default -> Locale.US;
        };
    }

    private static ResourceBundle getBundle(Locale locale) {
        return ResourceBundle.getBundle("Translations", locale, new UTF8Control());
    }

    private static int readNonNegativeInt(Scanner scanner, String prompt, ResourceBundle bundle) {
        while (true) {
            System.out.println(prompt);
            String line = scanner.nextLine();
            int value = Integer.parseInt(line.trim());
            if (value < 0) {
                throw new NumberFormatException("Number should not be negative");
            }
            return value;

        }
    }

    private static double readNonNegativeDouble(Scanner scanner, String prompt, ResourceBundle bundle) {
        while (true) {
            System.out.println(prompt);
            String line = scanner.nextLine();
            double value = Double.parseDouble(line.trim());
            if (value < 0) {
                throw new NumberFormatException("Number should not be negative");
            }
            return value;

        }
    }

    public static class UTF8Control extends ResourceBundle.Control {
        @Override
        public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
                throws IOException {
            String bundleName = toBundleName(baseName, locale);
            String resourceName = toResourceName(bundleName, "properties");
            try (InputStream stream = loader.getResourceAsStream(resourceName)) {
                if (stream == null) return null;
                return new PropertyResourceBundle(new java.io.InputStreamReader(stream, StandardCharsets.UTF_8));
            }
        }
    }
}
