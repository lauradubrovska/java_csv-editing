//Laura Dubrovska 
import java.io.*;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in); 
        String command;
    
        do {
            System.out.println("Enter command (print, add, del, edit, sort, find, avg, exit):");
            command = scanner.nextLine();
    
            // sadala find del edit add
            String[] commandParts = command.split(" ");
            if (commandParts.length > 1 && (commandParts[0].equals("find") || commandParts[0].equals("edit") || commandParts[0].equals("del") || commandParts[0].equals("add"))) {
                String commandType = commandParts[0];
                String data = command.substring(commandType.length() + 1); // atrod pec command visus datus
                switch (commandType) {
                    case "find":
                        double maxPrice;
                        try {
                            maxPrice = Double.parseDouble(data);
                            findTripsByPrice(maxPrice);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid price format. Please enter a valid number.");
                        }
                        break;
                    case "edit":
                        editTripInDatabase(data);
                        break;
                    case "del":
                        deleteTripFromDatabase(data);
                        break;
                    case "add":
                        addTripToDatabase(data);
                        break;
                    default:
                        System.out.println("Invalid command type.");
                }
            } else {
                switch (command) {
                    case "print":
                        printDatabase();
                        break;
                    case "sort":
                        sortDatabaseByDate();
                        System.out.println("Sorted");
                        break;
                    case "avg":
                        calculateAveragePrice();
                        break;
                    case "exit":
                        System.out.println("Exiting program.");
                        break;
                    default:
                        System.out.println("Invalid command. Please try again.");
                }
            }
        } while (!command.equals("exit"));
    
        scanner.close();
    }
    

    public static void printDatabase() {
        printSeparatorLine(60);
        String[] headers = {"ID", "City", "Date", "Days", "Price", " Vehicle"};
        int[] columnWidths = {4, 21, 11, 6, 10, 8};

        printTableHeader(headers, columnWidths);

        printSeparatorLine(60);
  
        try (Scanner fileScanner = new Scanner(new FileInputStream("db.csv"))) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                printTripInfo(line, columnWidths);
            }
        } catch (IOException e) {
            System.out.println("An error occurred while reading the database");
            e.printStackTrace();
        }
        printSeparatorLine(60);
    }    
    
    public static void printTableHeader(String[] headers, int[] columnWidths) {
        StringBuilder header = new StringBuilder();
        for (int i = 0; i < headers.length; i++) {
            if (i == headers.length - 1) {
                header.append(String.format("%-" + (columnWidths[i] - 1) + "s", headers[i]));
            } else if (i == 3 || i == 4) { // Align "Days" and "Price" headers to the right
                header.append(String.format("%" + columnWidths[i] + "s", headers[i]));
            } else {
                header.append(String.format("%-" + columnWidths[i] + "s", headers[i]));
            }
        }
        System.out.println(header.toString());
    }
    
    public static void printSeparatorLine(int length) {
        StringBuilder separator = new StringBuilder();
        for (int i = 0; i < length; i++) {
            separator.append("-");
        }
        System.out.println(separator.toString());
    }

    public static void printTripInfo(String tripData, int[] columnWidths) {
        String[] fields = tripData.split(";");
        StringBuilder tripInfo = new StringBuilder();
    
        for (int i = 0; i < fields.length; i++) {
            switch (i) {
                case 0: // ID
                    tripInfo.append(String.format("%-" + columnWidths[i] + "s", fields[i]));
                    break;
                case 1: // City
                    tripInfo.append(String.format("%-" + columnWidths[i] + "s", fields[i]));
                    break;
                case 2: // Date
                    tripInfo.append(String.format("%-" + columnWidths[i] + "s", fields[i]));
                    break;
                case 3: // Days
                    String days = fields[i];
                    int spacesToAdd = columnWidths[i] - days.length();
                    tripInfo.append(" ".repeat(Math.max(0, spacesToAdd)));
                    tripInfo.append(days);
                    break;
                case 4: // Price
                    String price = String.format("%" + columnWidths[i] + ".2f", Double.parseDouble(fields[i]));
                    tripInfo.append(price.substring(0, Math.min(price.length(), columnWidths[i])));
                    break;
                case 5: // Vehicle
                    tripInfo.append(" ");
                    tripInfo.append(String.format("%-" + (columnWidths[i] - 1) + "s", fields[i]));
                    break;
            }
        }
        System.out.println(tripInfo.toString());
    }
      
    public static void addTripToDatabase(String tripData) {

        String[] fields = tripData.split(";");

        if (fields.length != 6) {
            System.out.println("Wrong field count");
            return;
        }

        String id = fields[0];
        if (!id.matches("\\d{3}")) {
            System.out.println("Wrong ID format");
            return;
        }

        if (!isIdUnique(id)) {
            System.out.println("Trip with the same identifier already exists");
            return;
        }

        String city = capitalizeCity(fields[1]);

        if (!isValidDate(fields[2])) {
            System.out.println("Wrong date format");
            return;
        }

        try {
            int days = Integer.parseInt(fields[3]);
            if (days < 1) {
                System.out.println("Wrong day count");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Wrong day count format");
            return;
        }

        try {
            double price = Double.parseDouble(fields[4]);
            if (price <= 0) {
                System.out.println("Wrong price");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Wrong price format");
            return;
        }

        String vehicle = fields[5].toUpperCase();
        if (!isValidVehicle(vehicle)) {
            System.out.println("Wrong vehicle");
            return;
        }

        String formattedTripData = id + ";" + city + ";" + fields[2] + ";" + fields[3] + ";" + fields[4] + ";" + vehicle;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("db.csv", true))) {
            writer.write(formattedTripData + "\n");
            System.out.println("Added");
        } catch (IOException e) {
            System.out.println("An error occurred while adding the trip to the database");
            e.printStackTrace();
        }
    }

    public static void deleteTripFromDatabase(String tripId) {
        // vai id pareizs
        if (!tripId.matches("\\d{3}")) {
            System.out.println("Wrong ID format");
            return;
        }
        //vai id ir
        boolean found = false;
        try (BufferedReader reader = new BufferedReader(new FileReader("db.csv"))) {
            StringBuilder updatedDatabase = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(";");
                if (fields[0].equals(tripId)) {
                    found = true;
                } else {
                    updatedDatabase.append(line).append("\n");
                }
            }

            if (!found) {
                System.out.println("Trip with the specified identifier does not exist");
                return;
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter("db.csv"))) {
                writer.write(updatedDatabase.toString());
                System.out.println("Deleted");
            } catch (IOException e) {
                System.out.println("An error occurred while updating the database");
                e.printStackTrace();
            }
        } catch (IOException e) {
            System.out.println("An error occurred while reading the database");
            e.printStackTrace();
        }
    }

    public static void editTripInDatabase(String editData) {
    
        String[] fields = editData.split(";");

        if (fields.length < 6) {
            System.out.println("Wrong field count");
            return;
        }

        String id = fields[0];
        if (!id.matches("\\d{3}")) {
            System.out.println("Wrong ID format");
            return;
        }

        // parbauda id
        boolean found = false;
        try (BufferedReader reader = new BufferedReader(new FileReader("db.csv"))) {
            StringBuilder updatedDatabase = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tripFields = line.split(";");
                if (tripFields[0].equals(id)) {
                    found = true;
                    // update
                    String updatedTripData = updateTripData(tripFields, fields);
                    updatedDatabase.append(updatedTripData).append("\n");
                } else {
                    updatedDatabase.append(line).append("\n");
                }
            }

            if (!found) {
                System.out.println("Trip with the specified identifier does not exist");
                return;
            }

            //pieraksta file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("db.csv"))) {
                writer.write(updatedDatabase.toString());
                System.out.println("Changed");
            } catch (IOException e) {
                System.out.println("An error occurred while updating the database");
                e.printStackTrace();
            }
        } catch (IOException e) {
            System.out.println("An error occurred while reading the database");
            e.printStackTrace();
        }
    }

    public static boolean isIdUnique(String id) {
        try (BufferedReader reader = new BufferedReader(new FileReader("db.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(";");
                if (fields[0].equals(id)) {
                    return false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static String capitalizeCity(String city) {
        StringBuilder sb = new StringBuilder();
        String[] words = city.split(" ");
        for (String word : words) {
            sb.append(Character.toUpperCase(word.charAt(0)));
            sb.append(word.substring(1).toLowerCase());
            sb.append(" ");
        }
        return sb.toString().trim();
    }

    public static boolean isValidDate(String date) {
        String[] parts = date.split("/");
        if (parts.length != 3)
            return false;

        int day, month, year;
        try {
            day = Integer.parseInt(parts[0]);
            month = Integer.parseInt(parts[1]);
            year = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            return false;
        }

        if (day < 1 || day > 31 || month < 1 || month > 12)
            return false;

        return true;
    }

    public static boolean isValidVehicle(String vehicle) {
        return vehicle.equals("TRAIN") || vehicle.equals("PLANE") || vehicle.equals("BUS") || vehicle.equals("BOAT");
    }

    public static String updateTripData(String[] tripFields, String[] editFields) {
        StringBuilder updatedTripData = new StringBuilder();
        for (int i = 0; i < tripFields.length; i++) {
            if (i == 0) { // ID
                updatedTripData.append(tripFields[i]);
            } else {

                if (i >= editFields.length || editFields[i].isEmpty()) {
                    updatedTripData.append(tripFields[i]);
                } else {
                    if (i == 1) { // City
                        updatedTripData.append(capitalizeCity(editFields[i]));
                    } else if (i == 2) { // Date
                        if (isValidDate(editFields[i])) {
                            updatedTripData.append(editFields[i]);
                        } else {
                            System.out.println("Wrong date format");
                            return null;
                        }
                    } else if (i == 3) { // Days
                        try {
                            int days = Integer.parseInt(editFields[i]);
                            if (days < 1) {
                                System.out.println("Wrong day count");
                                return null;
                            } else {
                                updatedTripData.append(editFields[i]);
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Wrong day count format");
                            return null;
                        }
                    } else if (i == 4) { // cena
                        try {
                            double price = Double.parseDouble(editFields[i]);
                            if (price <= 0) {
                                System.out.println("Wrong price");
                                return null;
                            } else {
                                updatedTripData.append(editFields[i]);
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Wrong price format");
                            return null;
                        }
                    } else if (i == 5) { //vehicle
                        String vehicle = editFields[i].toUpperCase();
                        if (!isValidVehicle(vehicle)) {
                            System.out.println("Wrong vehicle");
                            return null;
                        } else {
                            updatedTripData.append(vehicle);
                        }
                    }
                }
            }
            if (i < tripFields.length - 1) {
                updatedTripData.append(";");
            }
        }
        return updatedTripData.toString();
    } 
    public static void sortDatabaseByDate() {
        List<String> trips = new ArrayList<>();
    
        try (BufferedReader reader = new BufferedReader(new FileReader("db.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                trips.add(line);
            }
        } catch (IOException e) {
            System.out.println("An error occurred while reading the database");
            e.printStackTrace();
            return;
        }
    
        // Bubble sort 
        for (int i = 0; i < trips.size() - 1; i++) {
            for (int j = 0; j < trips.size() - i - 1; j++) {
                String trip1 = trips.get(j);
                String trip2 = trips.get(j + 1);
                String[] dateParts1 = trip1.split(";")[2].split("/");
                String[] dateParts2 = trip2.split(";")[2].split("/");
                int year1 = Integer.parseInt(dateParts1[2]);
                int month1 = Integer.parseInt(dateParts1[1]);
                int day1 = Integer.parseInt(dateParts1[0]);
                int year2 = Integer.parseInt(dateParts2[2]);
                int month2 = Integer.parseInt(dateParts2[1]);
                int day2 = Integer.parseInt(dateParts2[0]);
                
                if (year1 > year2 || (year1 == year2 && month1 > month2) || (year1 == year2 && month1 == month2 && day1 > day2)) {
                    // apmaina vietam
                    String temp = trips.get(j);
                    trips.set(j, trips.get(j + 1));
                    trips.set(j + 1, temp);
                }
            }
        }
    
        // ieraksta faila atpakal
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("db.csv"))) {
            for (String trip : trips) {
                writer.write(trip + "\n");
            }
        } catch (IOException e) {
            System.out.println("An error occurred while writing the sorted database");
            e.printStackTrace();
        }
    }   
    public static void findTripsByPrice(double maxPrice) {
      
        printDatabaseHeader();
  
        int[] columnWidths = {4, 21, 11, 6, 10, 8};
    
        try (Scanner fileScanner = new Scanner(new FileInputStream("db.csv"))) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                String[] fields = line.split(";");
                double price = Double.parseDouble(fields[4]);
                if (price <= maxPrice) {
                    printTripInfo(line, columnWidths);
                }
            }
        } catch (IOException e) {
            System.out.println("An error occurred while reading the database");
            e.printStackTrace();
        }
    
        printSeparatorLine(60);
    }
    
    public static void printDatabaseHeader() {
        printSeparatorLine(60);
        
        String[] headers = {"ID", "City", "Date", "Days", "Price", " Vehicle"};
   
        int[] columnWidths = {4, 21, 11, 6, 10, 8};
    
        printTableHeader(headers, columnWidths);
    
        printSeparatorLine(60);
    }
    public static void calculateAveragePrice() {
        double totalPrice = 0;
        int count = 0;
        try (Scanner fileScanner = new Scanner(new FileInputStream("db.csv"))) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                String[] fields = line.split(";");
                double price = Double.parseDouble(fields[4]);
                totalPrice += price;
                count++;
            }
            if (count > 0) {
                double averagePrice = totalPrice / count;
                System.out.printf("average=%.2f\n", averagePrice);
            } else {
                System.out.println("No trips found in the database.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred while reading the database");
            e.printStackTrace();
        }
    }
}
