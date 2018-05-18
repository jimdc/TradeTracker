# TradeTracker

## Functional Specification [?](https://www.joelonsoftware.com/whattimeisit/)

### Overview
**TradeTracker** is a mobile app that alerts you to personally-relevant changes in the share prices of stocks and cryptocurrencies. 

This spec is not complete, and it does not discuss algorithmic/implementation details. It simply discusses what the user sees when they interact with the app.

### Scenarios
#### Scenario 1: Jean-Pierre
Jean-Pierre is a busy college student. He has a great intuitive sense of when a stock will rebound after a fall, since he spends many hours looking at share price trends. But sometimes, significant price events will happen to penny stocks in which he owns shares; while he sleeps. 

Jean-Pierre does not mind being woken up about these price events, if it means averting losses. Since he uses Investopedia's "trailing stop loss" technique; a natural Google search leads him to **TradeTracker**.

JP wants to set a limit on his maximum possible loss while not limiting his maximum possible gain. He takes the $50 he invests in an Israeli agrochemical startup, and tells **TradeTracker**: alert me if the price dips by 2%. Originally, that's 50->49, but it goes up to 100. Now TradeTracker is sensitive to if it goes from 100->98. (Is this how it works?)

#### Scenario 2: Subbaraman
Subbaraman is a millenial Québécoise barista who doesn't trust banks, and so she spreads her savings into various cryptocurrencies. She is OK if the prices all fall; but if one falls or rises rapidly to another, she needs to spend more time attending to her portfolio.

(... somehow she finds **TradeTracker** and uses some very specific features ...)

### Non Goals
This version will *not* support the following features:
* Make trades for you
* Analyze historical data
* Suggest how you should trade

### TradeTracker Flowchart
(storyboard picture goes here)

### Screen by Screen specification
**TradeTracker** has several screens, and all of them are written in XML using Android's standard Material Design library.

#### Stock alert list
...The entries are color-coded as stock or crypto...

(screenshot goes here)

#### Add/edit stock
...the user specifies the ticker name and target...

(screenshot goes here)

#### Advanced add/edit
...trailing stop loss and so forth...

(screenshot goes here)

#### Snooze
...the user can tell the app to stop scanning for a few hours, but then to resume...

(screenshot goes here)

#### Settings
...the user can specify how often TradeTracker should query the prices from the net...

(screenshot goes here)

## Technical Specification

(It could be a page on the Advent website)