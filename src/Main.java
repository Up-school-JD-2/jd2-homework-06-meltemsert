import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;

public class Main {

    public static void main(String[] args) {
        ProductManager manager = new ProductManager();
        Random random = new Random();
        manager.addProduct(new Product("1", "Product 1", 10, 9.99, "Category 1", ProductStatus.ACTIVE));
        manager.addProduct(new Product("2", "Product 2", 5, 14.99, "Category 2", ProductStatus.ACTIVE));
        manager.addProduct(new Product("3", "Product 3", 0, 19.99, "Category 1", ProductStatus.OUT_OF_STOCK));
        manager.addProduct(new Product("4", "Product 4", 3, 24.99, "Category 2", ProductStatus.DISCONTINUED));
        // ORD-122
        manager.registerOrderNumberSupplier("supplier-1", () -> {
            int orderNumber = random.nextInt(1000);
            return "ORD-" + orderNumber;
        });
        // Supplier<String> tempSupplier = ()-> "Asım";
        //ORD-202306041426
        manager.registerOrderNumberSupplier("supplier-2", () -> {
            LocalDateTime now = LocalDateTime.now();
            String orderNumber = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(now);
            return "ORD-" + orderNumber;
        });
        manager.registerOrderNumberSupplier("supplier-3", () -> UUID.randomUUID().toString());

        System.out.println("UPDATE STOCK ");
        manager.updateStock("1", 20, (product, quantity) -> {
            int newStock = product.getStock() + quantity;
            product.setStock(newStock);
        });

        System.out.println("\n\n\n\n");

        System.out.println("GENERATE ORDER NUMBER ");
        String supplier1OrderId = manager.generateOrderNumber("supplier-1");
        String supplier2OrderId = manager.generateOrderNumber("supplier-2");
        String supplier3OrderId = manager.generateOrderNumber("supplier-3");
        String unknownSupplierOrderId = manager.generateOrderNumber("Supplier-3");
        System.out.println("supplier-1, generated order ID : " + supplier1OrderId);
        System.out.println("supplier-2, generated order ID : " + supplier2OrderId);
        System.out.println("supplier-3, generated order ID : " + supplier3OrderId);
        System.out.println("unknownSupplierOrderId : " + unknownSupplierOrderId);

        Map<String, Integer> orderItems = new HashMap<>();
        orderItems.put("1", 3);
        orderItems.put("2", 2);
        manager.processOrder(supplier1OrderId, orderItems, (product, quantity) -> {
            int newStock = product.getStock() - quantity;
            product.setStock(newStock);
        });

        double totalValue = manager.calculateTotalValue(product -> product.getPrice() * product.getStock());
        System.out.println("Total value of products: " + totalValue);

        var singleTotalPrice = manager.calculateTotalValue(Product::getPrice);
        System.out.println("single Total price" + singleTotalPrice);

        System.out.println("\n\nFILTER PRODUCTS");
        List<Product> filteredProducts =
                manager.filterProducts(
                        product -> ProductStatus.ACTIVE.equals(product.getProductStatus()) || ProductStatus.OUT_OF_STOCK.equals(
                                product.getProductStatus()));
        System.out.println("Active products :");
        filteredProducts.forEach(System.out::println);


        System.out.println("*********************************");
        Product p1 = new Product("111", "prod1", 10, 112.33, "category1", ProductStatus.ACTIVE);
        Product p2 = new Product("222", "prod2", 20, 34.66, "category1", ProductStatus.ACTIVE);
        Product p3 = new Product("333", "prod3", 30, 33.33, "category3", ProductStatus.OUT_OF_STOCK);
        Product p4 = new Product("444", "prod4", 40, 55.33, "category3", ProductStatus.ACTIVE);

        Map<String, Product> products = new HashMap<>();
        products.put("1", p1);
        products.put("2", p2);
        products.put("3", p3);
        products.put("4", p4);
        List<Product> activeProducts = products.values().stream()
                .filter(product -> (product.getProductStatus().equals(ProductStatus.ACTIVE)))
                .sorted(Comparator.comparingDouble(Product::getPrice)).toList();
        System.out.println(activeProducts);

        OptionalDouble productAverage = products.values().stream()
                .filter(product -> product.getCategory().equals("category1"))
                .mapToDouble(Product::getPrice).average();
        System.out.println(productAverage);
    }
}