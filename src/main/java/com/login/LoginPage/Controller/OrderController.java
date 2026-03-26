package com.login.LoginPage.Controller;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.login.LoginPage.model.Bill;
import com.login.LoginPage.model.MenuItem;
import com.login.LoginPage.model.OrderItem;
import com.login.LoginPage.order.RestaurantOrder;
import com.login.LoginPage.repository.BillRepository;
import com.login.LoginPage.repository.MenuItemRepository;
import com.login.LoginPage.repository.OrderRepository;

@Controller
public class OrderController {

    @Autowired    private OrderRepository orderRepository;

    @Autowired    private MenuItemRepository menuItemRepository;
    
    @Autowired    private BillRepository billRepository;

    @PostMapping("/order")
    public String submitOrder(@ModelAttribute("order") RestaurantOrder order, RedirectAttributes redirectAttributes) {
        // 1. Filter out items where quantity is 0
        List<OrderItem> selectedItems = order.getItems().stream()
                .filter(item -> item.getQuantity() != null && item.getQuantity() > 0)
                .collect(Collectors.toList());

        if (selectedItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select at least one item.");
            return "redirect:/order";
        }

        // 2. Set remaining order details
        order.setItems(selectedItems);
        order.setOrderTime(LocalDateTime.now());
        order.setStatus("PENDING");

        // 3. Calculate total amount
        double total = selectedItems.stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();
        order.setTotalAmount(total);

        // 4. Save to database
        orderRepository.save(order);

        redirectAttributes.addFlashAttribute("success", "Order sent to kitchen successfully!");
        return "redirect:/active-orders";
    }
    
    @GetMapping("/order")
    public String showOrderPage(Model model) {
    	List<MenuItem> allMenuItems = menuItemRepository.findAll().stream()
                .filter(MenuItem::isAvailable) 
                .collect(Collectors.toList());
        
        RestaurantOrder order = new RestaurantOrder();
        List<OrderItem> formItems = allMenuItems.stream()
                .map(menuItem -> {
                    OrderItem oi = new OrderItem();
                    oi.setName(menuItem.getName());
                    oi.setPrice(menuItem.getPrice());
                    oi.setQuantity(0); // Initialize with 0
                    return oi;
                }).collect(Collectors.toList());
        
        order.setItems(formItems);
        model.addAttribute("order", order);
        return "order";
    }

    @PostMapping("/save-order")
    public String saveOrder(@ModelAttribute("order") RestaurantOrder order, RedirectAttributes redirectAttributes) {
        // 1. Filter out items that weren't ordered (quantity is 0 or null)
        List<OrderItem> orderedItems = order.getItems().stream()
                .filter(item -> item.getQuantity() != null && item.getQuantity() > 0)
                .collect(Collectors.toList());

        if (orderedItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please add at least one item to the order.");
            return "redirect:/order";
        }

        // 2. Set metadata
        order.setItems(orderedItems);
        order.setOrderTime(LocalDateTime.now());
        order.setStatus("PENDING");

        // 3. Save to DB
        orderRepository.save(order);

        // 4. Send success message to the welcome screen
        redirectAttributes.addFlashAttribute("success", "Order for Table " + order.getTableNumber() + " saved successfully!");
        return "redirect:/welcome";
    }
    
    @GetMapping("/print-bill/{id}")
    public String printBill(@PathVariable Long id, Model model) {
        RestaurantOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Order ID"));
        
        // Recalculate total just in case it wasn't saved yet
        double total = order.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        
        model.addAttribute("order", order);
        model.addAttribute("total", total);
        return "bill-receipt"; 
    }
    
    @PostMapping("/order/accept")
    public String acceptOrder(@RequestParam("orderId") Long id) {
        orderRepository.findById(id).ifPresent(order -> {
            order.setStatus("ACCEPTED");
            order.setAcceptedAt(LocalDateTime.now()); // Set the acceptance timestamp
            orderRepository.save(order);
        });
        return "redirect:/active-orders";
    }
    
    @PostMapping("/order/complete")
    public String completeOrder(@RequestParam("orderId") Long id) {
        orderRepository.findById(id).ifPresent(order -> {
            order.setStatus("COMPLETED");
            orderRepository.save(order);
        });
        return "redirect:/active-orders";
    }

    @GetMapping("/active-orders")
    public String listActiveOrders(Model model) {
        // Modify the filter to include BOTH PENDING and ACCEPTED statuses
        List<RestaurantOrder> active = orderRepository.findAll().stream()
                .filter(o -> "PENDING".equals(o.getStatus()) || "ACCEPTED".equals(o.getStatus()))
                .collect(Collectors.toList());
        
        model.addAttribute("orders", active);
        return "active-orders";
    }

  
    // 2. Updated Consolidated Bill Logic
    @GetMapping("/table/bill/{tableNumber}")
    public String generateTableBill(@PathVariable Integer tableNumber, Model model) {
        // Fetch all orders for this table that aren't PAID yet
        List<RestaurantOrder> tableOrders = orderRepository.findAll().stream()
                .filter(o -> o.getTableNumber().equals(tableNumber) && !"PAID".equals(o.getStatus()))
                .collect(Collectors.toList());

        if (tableOrders.isEmpty()) {
            return "redirect:/welcome?error=NoOrders";
        }

        // UNIQUE BILL NUMBER GENERATION
        // Format: BN - YYYYMMDD - TableNumber - Last 4 digits of timestamp
        String datePart = java.time.LocalDate.now().toString().replace("-", "");
        String uniqueSuffix = String.valueOf(System.currentTimeMillis()).substring(9);
        String billNumber = "BN-" + datePart + "-" + tableNumber + "-" + uniqueSuffix;

        // Calculate Totals with GST
        double subTotal = tableOrders.stream()
                .flatMap(o -> o.getItems().stream())
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();
        double taxAmount = subTotal * 0.05; // 5% GST
        double grandTotal = subTotal + taxAmount;

        model.addAttribute("billNumber", billNumber);
        model.addAttribute("tableNumber", tableNumber);
        model.addAttribute("items", tableOrders.stream().flatMap(o -> o.getItems().stream()).collect(Collectors.toList()));
        model.addAttribute("subTotal", subTotal);
        model.addAttribute("taxAmount", taxAmount);
        model.addAttribute("total", grandTotal);
        
        // Join IDs for the Checkout process
        String orderIds = tableOrders.stream().map(o -> String.valueOf(o.getId())).collect(Collectors.joining(","));
        model.addAttribute("orderIds", orderIds);

        return "table-bill"; 
    }

 // 3. Final Checkout (Clears the table for next customer)
    @PostMapping("/table/checkout")
    public String checkoutTable(@RequestParam String orderIds, 
                                @RequestParam String billNumber,
                                @RequestParam Double subTotal,
                                @RequestParam Double taxAmount,
                                @RequestParam Double total,
                                @RequestParam Integer tableNumber) {
        
        // Create and Save the Permanent Bill Record
        Bill bill = new Bill();
        bill.setBillNumber(billNumber);
        bill.setTableNumber(tableNumber);
        bill.setCheckoutTime(LocalDateTime.now());
        bill.setSubTotal(subTotal);
        bill.setTaxAmount(taxAmount);
        bill.setGrandTotal(total);
        
        List<OrderItem> allItemsForBill = new ArrayList<>();
        for (String id : orderIds.split(",")) {
            orderRepository.findById(Long.parseLong(id)).ifPresent(order -> {
            	allItemsForBill.addAll(order.getItems());
                order.setStatus("PAID");
                orderRepository.save(order);
            });
        }
        bill.setItems(allItemsForBill);
        billRepository.save(bill); 
        return "redirect:/welcome?success=TableCheckedOut";
    }
    
    @GetMapping("/bill/history")
    public String viewBillHistory(
            @RequestParam(required = false) String billNumber,
            @RequestParam(required = false) Integer tableNumber,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate,
            Model model) {

        List<Bill> allBills = billRepository.findAllByOrderByCheckoutTimeDesc();

        // Apply filters in memory
        List<Bill> filteredBills = allBills.stream()
                .filter(bill -> (billNumber == null || billNumber.isEmpty() || bill.getBillNumber().contains(billNumber)))
                .filter(bill -> (tableNumber == null || bill.getTableNumber().equals(tableNumber)))
                .filter(bill -> {
                    if (startDate == null) return true;
                    return !bill.getCheckoutTime().toLocalDate().isBefore(startDate);
                })
                .filter(bill -> {
                    if (endDate == null) return true;
                    return !bill.getCheckoutTime().toLocalDate().isAfter(endDate);
                })
                .collect(Collectors.toList());

        model.addAttribute("bills", filteredBills);
        return "bill-history";
    }

    @GetMapping("/bill/view/{id}")
    public String viewSavedBill(@PathVariable Long id, Model model) {
        Bill bill = billRepository.findById(id).orElseThrow();
        
        // Ensure the items are loaded from the bill object
        List<OrderItem> billItems = bill.getItems();
        
        model.addAttribute("bill", bill);
        model.addAttribute("items", billItems); 
        model.addAttribute("tableNumber", bill.getTableNumber());
        model.addAttribute("subTotal", bill.getSubTotal());
        model.addAttribute("taxAmount", bill.getTaxAmount());
        model.addAttribute("total", bill.getGrandTotal());
        
        return "saved-bill-detail"; 
    }

    @GetMapping("/completed-orders")
    public String listCompletedOrders(Model model) {
        List<RestaurantOrder> completed = orderRepository.findAll().stream()
                .filter(o -> "COMPLETED".equals(o.getStatus()))
                .collect(Collectors.toList());
        model.addAttribute("orders", completed);
        return "completed-orders";
    }
    
 // 1. Handle the search from the Welcome Screen
    @GetMapping("/table/bill/search")
    public String searchTableBill(@RequestParam Integer tableNumber) {
        return "redirect:/table/bill/" + tableNumber;
    }

   
    @GetMapping("/table-map")
    public String showTableMap(Model model) {
        // Fetch all orders that are not yet PAID
        List<RestaurantOrder> activeOrders = orderRepository.findAll().stream()
                .filter(o -> !"PAID".equals(o.getStatus()))
                .collect(Collectors.toList());

        // Map to store Status for each table (1 to 10)
        java.util.Map<Integer, String> tableStatusMap = new java.util.HashMap<>();
        
        for (int i = 1; i <= 10; i++) {
            final int tableNum = i;
            List<RestaurantOrder> ordersForTable = activeOrders.stream()
                    .filter(o -> o.getTableNumber() == tableNum)
                    .collect(Collectors.toList());

            if (ordersForTable.isEmpty()) {
                tableStatusMap.put(i, "AVAILABLE"); // Green
            } else if (ordersForTable.stream().anyMatch(o -> "PENDING".equals(o.getStatus()))) {
                tableStatusMap.put(i, "PENDING");   // Yellow
            } else {
                tableStatusMap.put(i, "OCCUPIED");  // Red
            }
        }

        model.addAttribute("tableStatusMap", tableStatusMap);
        return "table-map";
    }
    
    
}