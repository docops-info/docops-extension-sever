Given(/^I am on the login page$/) do
  pending
end

When(/^I enter a valid username and password$/) do
  pending
end

Then(/^I should be redirected to the homepage$/) do
  pending
end

And(/^I should see a welcome message$/) do
  pending
end

When(/^I enter an invalid username and password$/) do
  pending
end

Then(/^I should see an error message$/) do
  pending
end

When(/^I enter username  and password$/) do
  pending
end

Then(/^I should see the message$/) do
  pending
end

Given(/^the user is on the login page$/) do
  pending
end

When(/^they enter valid credentials$/) do
  pending
end

Then(/^they should be redirected to dashboard$/) do
  pending
end

And(/^the login attempt should be logged$/) do
  pending
end

When(/^they enter invalid credentials$/) do
  pending
end

Then(/^they should see an error message$/) do
  pending
end

And(/^they should remain on the login page$/) do
  pending
end

Given(/^a logged\-in shopper with items in the cart$/) do
  pending
end

And(/^they have a valid 10% coupon$/) do
  pending
end

When(/^they apply the coupon at checkout$/) do
  pending
end

Then(/^the subtotal should reflect a 10% discount$/) do
  pending
end

And(/^the member discount should be applied afterwards$/) do
  pending
end

But(/^the total should not go below \$0\.(\d+)$/) do |arg|
  pending
end

Given(/^a guest user on the checkout page$/) do
  pending
end

When(/^they enter an expired coupon code$/) do
  pending
end

Then(/^they should see an error explaining the coupon is expired$/) do
  pending
end

And(/^the cart total should remain unchanged$/) do
  pending
end

Given(/^the downstream payment service is failing$/) do
  pending
end

When(/^three consecutive calls fail$/) do
  pending
end

Then(/^the circuit breaker should open$/) do
  pending
end

And(/^subsequent calls should be short\-circuited$/) do
  pending
end

Given(/^the circuit breaker is open for 60s$/) do
  pending
end

When(/^the cooldown elapses$/) do
  pending
end

Then(/^a single probe request should be allowed$/) do
  pending
end

And(/^if it succeeds the circuit should close$/) do
  pending
end

But(/^if it fails the circuit should stay open$/) do
  pending
end

Given(/^a user is on the search page$/) do
  pending
end

When(/^they type the first three characters$/) do
  pending
end

Then(/^the top (\d+) matching suggestions should appear$/) do |arg|
  pending
end

And(/^selecting a suggestion should populate the input$/) do
  pending
end