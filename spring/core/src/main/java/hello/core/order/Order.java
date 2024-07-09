package hello.core.order;

public class Order {
    private Long memberId;
    private String itemName;
    private int itemPrice;
    private int discountPolicy;

    public Order(Long memberId, String itemName, int itemPrice, int discountPolicy) {
        this.memberId = memberId;
        this.itemName = itemName;
        this.itemPrice = itemPrice;
        this.discountPolicy = discountPolicy;
    }

    public int calculatePrice(){
        return itemPrice - discountPolicy;
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getItemName() {
        return itemName;
    }

    public int getItemPrice() {
        return itemPrice;
    }

    public int getDiscountPolicy() {
        return discountPolicy;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setItemPrice(int itemPrice) {
        this.itemPrice = itemPrice;
    }

    public void setDiscountPolicy(int discountPolicy) {
        this.discountPolicy = discountPolicy;
    }

    @Override
    public String toString() {
        return "Order{" +
                "memberId=" + memberId +
                ", itemName='" + itemName + '\'' +
                ", itemPrice=" + itemPrice +
                ", discountPolicy=" + discountPolicy +
                '}';
    }
}
