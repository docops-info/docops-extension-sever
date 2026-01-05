Feature: Shopping Cart Discounts
  Scenario: Apply Coupon and Member Discount
    Given a logged-in shopper with items in the cart
    And they have a valid 10% coupon
    When they apply the coupon at checkout
    Then the subtotal should reflect a 10% discount
    And the member discount should be applied afterwards
    But the total should not go below $0.00

  Scenario: Expired Coupon Rejected
    Given a guest user on the checkout page
    When they enter an expired coupon code
    Then they should see an error explaining the coupon is expired
    And the cart total should remain unchanged