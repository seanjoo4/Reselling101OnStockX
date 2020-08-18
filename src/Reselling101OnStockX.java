import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class Reselling101OnStockX {

    private static DecimalFormat df = new DecimalFormat("0.00");

    public static void main(String[] args) throws IOException {

        Scanner sc = new Scanner(System.in);
        String url = "";
        String shoe = "";
        String input = "";

        // constants
        final int AVG_PRICE_PREM = 48;
        final double PAYMENT_PROCESSING_FEE = .03;
        final double TRANSACTION_FEE_TIER_ONE = .095;
        final double TRANSACTION_FEE_TIER_TWO = .09;
        final double TRANSACTION_FEE_TIER_THREE = .085;
        final double TRANSACTION_FEE_TIER_FOUR = .08;

        // Welcome screen
        System.out.println("Hello, welcome to StockX's database of current shoe prices as well as completed sale data.");
        System.out.println("We will assist you in evaluating whether or not you should sell or hold.");
        boolean errorWithURL = true;
        boolean again = true;
        while (again) {
            while (errorWithURL) {

                // asking for model
                System.out.println("What sneaker do you want to search up? Please include the model and color way. Ex: Air Jordan 1 Retro High Court Purple White.");
                input = sc.nextLine();
                shoe = input;

                // constructing URL on StockX
                input = input.replaceAll(" ", "-");
                url = "https://stockx.com/" + input;

                // Checks if given URL is a valid URL
                URL u = new URL(url);
                HttpURLConnection huc = (HttpURLConnection) u.openConnection();
                huc.setRequestMethod("GET");
                huc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2 (.NET CLR 3.5.30729)");
                huc.connect();
                int code = huc.getResponseCode();
                if (code != 404) {
                    errorWithURL = false;
                    System.out.println("There is your URL: " + url);
                } else {
                    System.out.println("The entered URL is not valid, please reenter and be careful for any typos!");
                }
            }

            // Begin to web scrape
            Document doc = Jsoup.connect(url).get();
            Elements body = doc.select("ul.list-unstyled.sneakers");

            // Web scraping the title screen of the URL (Shoe name)
            String title = doc.title();
            System.out.println("StockX: " + title);

            // Web scraping all of the current buy now and highest bid prices for every size
            String price = "";
            List<String> arr = new ArrayList<>();
            for (Element e : doc.select("ul.list-unstyled.sneakers li")) {
                if (e.select(".inset").text().equals("")) {
                    continue;
                } else {
                    price = e.select(".inset").text();
                    arr.add(price);
                }
            }
            // parsing only the buy now prices
            int size = arr.size();
            int location = 0;
            for (int i = 1; i < arr.size(); i++) {
                if (arr.get(i).contains("All")) {
                    location = i;
                }
            }
            System.out.println("Current StockX Prices for Every Size for the " + shoe + ": " + arr.subList(0, size - location));

            // Web scraping the statistics of the sneaker
            Elements body2 = doc.select("div.gauges");
            List<String> statistic = new ArrayList<>();
            for (Element e : doc.select("div.gauges div")) {
                if (e.select("div.gauge-container").text().equals("")) {
                    continue;
                } else {
                    String info = e.select("div.gauge-container").text();
                    statistic.add(info);
                    System.out.println(info);
                }
            }

            // Parsing the premium percentage from the rest of the statistics
            int par = 0;
            int percentPos = 0;
            int numPos = 0;
            int pricePos = 0;
            for (int i = 0; i < statistic.size(); i++) {
                if (statistic.get(i).contains(") ")) {
                    par = i;
                }
                if (statistic.get(i).contains("%")) {
                    percentPos = i;
                }
                if (statistic.get(i).contains("Sales ")) {
                    numPos = i;
                }
                if (statistic.get(i).contains("Price")) {
                    pricePos = i;
                }
            }
            List<String> temp = statistic;
            int len = statistic.size();
            temp = temp.subList(numPos, len - pricePos);
            statistic = statistic.subList(par, len - percentPos);

            // Converting an ArrayList to a String
            StringBuilder sb = new StringBuilder();
            for (String s : statistic) {
                sb.append(s);
                sb.append("\t");
            }

            StringBuilder stringBuilder = new StringBuilder();
            for (String s : temp) {
                stringBuilder.append(s);
                stringBuilder.append("\t");
            }

            // Parse to only have number of Sales as a String
            int pos = stringBuilder.toString().indexOf("s ");
            String numOfSales = stringBuilder.substring(pos + 2, stringBuilder.length() - 1);

            // Parse to only have percentage of Price Premium in String format
            int position = sb.toString().indexOf(") ");
            String percent = sb.toString().substring(position + 2, sb.toString().length() - 2);

            // Convert String to int
            int numberOfSales = Integer.parseInt(numOfSales);
            double integerPercent = Double.parseDouble(percent);
            double premDifference = integerPercent - AVG_PRICE_PREM;
            double premRatio = (premDifference / AVG_PRICE_PREM) * 100;

            System.out.println("Interesting statistics! Let's see if you should sell or hold your " + shoe + "!");

            // Sees the difference between the ratio of the Premium Ratio of the given shoe to the average Premium Average on StockX (48%).
            if ((premDifference > 0) && (premDifference < 10)) {
                if (numberOfSales >= 9000) {
                    System.out.println(shoe + " is currently a good time to sell! There were already " + numberOfSales + " sales. Also, it is greater than the average shoe Price Premium on StockX by " + df.format(premRatio) + "!");
                } else if (numberOfSales > 4000) {
                    System.out.println(shoe + " is currently a decent time to sell! Note that there were " + numberOfSales + " sales to date. Also, it is greater than the average shoe Price Premium on StockX by " + df.format(premRatio) + "!");
                } else {
                    System.out.println(shoe + " is currently an okay time to sell! There were only " + numberOfSales + " sales, so this may be a slower sale. On the bright side, this shoe is greater than the average shoe Price Premium on StockX by " + df.format(premRatio) + "!");
                }
            } else if (premDifference == 0) {
                if (numberOfSales >= 9000) {
                    System.out.println(shoe + " is currently exactly at StockX's average shoe Price Premium: 48%. There were a lot of sales! Specifically, " + numberOfSales + " sales, but most likely the shoe's price will not rise in the near future. Probably best to sell now!");
                } else if (numberOfSales > 4000) {
                    System.out.println(shoe + " is currently exactly at StockX's average shoe Price Premium: 48%. There only have been " + numberOfSales + " sales, so most likely the shoe's price will not rise in the near future. Also, you will probably have to wait for someone to buy! Probably best to list now!");
                } else {
                    System.out.println(shoe + " is currently exactly at StockX's average shoe Price Premium: 48%. Unfortunately there only have been " + numberOfSales + " sales. This shoe will be a very slow sell! Try your best to get these off your hands! You can still make good profit!");
                }
            } else if (premDifference >= 10) {
                if (numberOfSales >= 9000) {
                    System.out.println("GREAT TIME TO SELL AND MOST LIKELY WILL CONTINUE TO RISE!!! ALREADY " + numberOfSales + " SALES! HIGHER THAN STOCKX'S PRICE PREMIUM BY " + df.format(premRatio) + "%!!!!!");
                } else if (numberOfSales > 4000) {
                    System.out.println("GREAT TIME TO SELL!!! DECENT AMOUNT OF SALES: " + numberOfSales + "! HIGHER THAN STOCKX'S PRICE PREMIUM BY " + df.format(premRatio) + "%!!!");
                } else {
                    System.out.println("GOOD TIME TO SELL!!! Only " + numberOfSales + " sales, so if you can hold, the demand may rise in the future! HIGHER THAN STOCKX'S PRICE PREMIUM BY " + df.format(premRatio) + "%!!!");
                }
            } else if ((premDifference < 0) && (premDifference > -10)) {
                if (numberOfSales >= 9000) {
                    System.out.println("There are currently " + numberOfSales + " sales. With these many sells you probably will not see a rise in the market. You will still be making a profit, but you are below StockX's Price Premium average by " + df.format(premRatio) + "%.");
                } else if (numberOfSales > 4000) {
                    System.out.println("There are currently " + numberOfSales + " sales. There are a decent amount of sales, so there will not be a rise in the market. You will most likely have to wait a bit for the shoe to sell, and you are below StockX's Price Premium average by " + df.format(premRatio) + "%.");
                } else {
                    System.out.println("There are currently " + numberOfSales + " sales. There are not a lot of sales, so there will not be a rise in the market. You will have to wait quite some time for the shoe to sell. You are below StockX's Price Premium average by " + df.format(premRatio) + "%, but still bringing in profit.");
                }
            } else if ((premDifference <= -10) && (integerPercent == 0)) {
                if (numberOfSales >= 9000) {
                    System.out.println("You are currently not making a profit right now. There also have been " + numberOfSales + " sales. Terrible time to sell unless you got the product below retail.");
                } else if (numberOfSales > 4000) {
                    System.out.println("You are currently not making a profit right now. There also have been a decent amount of sales: " + numberOfSales + ". Not the greatest time to sell unless you got the product below retail.");
                } else {
                    System.out.println("You are currently not making a profit right now. There also only have been " + numberOfSales + "amount of sales. Bad time to sell unless you got the product below retail.");
                }
            } else {
                if (numberOfSales >= 9000) {
                    System.out.println("Bad time to sell. There have been " + numberOfSales + ". You are losing " + df.format(premRatio) + "% With this many sales, I do not think there will be a rise any time soon. I would hold the shoe and hope to see a rise.");
                } else if (numberOfSales > 4000) {
                    System.out.println("Terrible time to sell. Only " + numberOfSales + " sales. You are losing " + df.format(premRatio) + "% Breaking even will probably be your best bet.");
                } else {
                    System.out.println("Horrible shoe to sell. There only have been " + numberOfSales + " sales. You are losing " + df.format(premRatio) + "% These are probably bricks.");
                }
            }

            // Asking user how much the user wants to sell the shoe for
            double revenue = 0.00;
            boolean confirm = true;
            while (confirm) {
                boolean invalid = true;
                while (invalid) {
                    System.out.println("How much are you selling it for? No dollar sign ($) please!");
                    try {
                        revenue = Double.parseDouble(sc.next());
                        invalid = false;
                        break;
                    } catch (NumberFormatException ignore) {
                        System.out.println("Invalid input! Please input a double!");
                        sc.nextLine();
                    }
                }
                sc.nextLine();
                System.out.println("Are you sure?");
                String responseNext = sc.nextLine();
                responseNext = responseNext.toLowerCase();
                if (responseNext.equals("yes")) {
                    confirm = false;
                }
                else if (!responseNext.equals("no")) {
                    System.out.println("Incorrect spelling! Please reenter!");
                }
                else {
                    continue;
                }
            }

            // Asking user what tier seller they are
            confirm = true;
            double retail = 0.00;
            int tier = 0;
            while (confirm) {
                boolean check = true;
                while (check) {
                    System.out.println("What tier seller are you (1, 2, 3, or 4)?");
                    try {
                        tier = Integer.parseInt(sc.next());
                        check = false;
                        if ((tier < 1) || (tier > 4)) {
                            System.out.println("Invalid input! Please input a valid integer!");
                            sc.nextLine();
                            check = true;
                        }
                        break;
                    } catch (NumberFormatException ignore) {
                        System.out.println("Invalid input! Please input a valid integer!");
                        sc.nextLine();
                    }
                }
                sc.nextLine();
                System.out.println("Are you sure?");
                String res = sc.nextLine();
                res = res.toLowerCase();
                if (res.equals("yes")) {
                    confirm = false;
                }
                else if (!res.equals("no")) {
                    System.out.println("Incorrect spelling! Please reenter!");
                }
                else {
                    continue;
                }
            }

            // Asking how much the user bought the shoe for.
            confirm = true;
            while (confirm) {
                boolean invalid = true;
                while (invalid) {
                    System.out.println("How much did you purchase your " + shoe + " for? No dollar sign ($) please!");
                    try {
                        retail = Double.parseDouble(sc.next());
                        invalid = false;
                        break;
                    } catch (NumberFormatException ignore) {
                        System.out.println("Invalid input! Please input a double!");
                        sc.nextLine();
                    }
                }
                sc.nextLine();
                System.out.println("Are you sure?");
                String responseNext = sc.nextLine();
                responseNext = responseNext.toLowerCase();
                if (responseNext.equals("yes")) {
                    confirm = false;
                }
                else if (!responseNext.equals("no")) {
                    System.out.println("Incorrect spelling! Please reenter!");
                }
                else {
                    continue;
                }
            }

            // Gathering Other Expenses based on user's input on seller tier level
            double grossProfit = revenue - retail;
            double otherExpenses = 0.00;
            double revnueCalc = revenue;
            switch (tier) {
                case 1:
                    otherExpenses = revnueCalc * (PAYMENT_PROCESSING_FEE + TRANSACTION_FEE_TIER_ONE);
                    break;
                case 2:
                    otherExpenses = revnueCalc * (PAYMENT_PROCESSING_FEE + TRANSACTION_FEE_TIER_TWO);
                    break;
                case 3:
                    otherExpenses = revnueCalc * (PAYMENT_PROCESSING_FEE + TRANSACTION_FEE_TIER_THREE);
                    break;
                case 4:
                    otherExpenses = revnueCalc * (PAYMENT_PROCESSING_FEE + TRANSACTION_FEE_TIER_FOUR);
                    break;
                default:
                    System.out.println("Invalid!");
                    break;
            }

            // Calculating the Gross Margin and Net Margin of the shoe that is currently being planned to sell.
            double grossMargin = (grossProfit / revenue) * 100;
            double netIncome = grossProfit - otherExpenses;
            double netMargin = (netIncome / revenue) * 100;
            System.out.println("Gross Margin: " + df.format(grossMargin) +"%");
            System.out.println("Net Margin: " + df.format(netMargin) + "%");

            // Ask if the user wants to search for another model.
            System.out.println("Would you like to search for another model?");
            String responseAgain = sc.nextLine();
            responseAgain = responseAgain.toLowerCase();
            if (responseAgain.equals("no")) {
                again = false;
                System.out.println("Thank you for using StockX's database!");
                break;
            } else {
                errorWithURL = true;
            }
        }
    }
}
