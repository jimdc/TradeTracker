## TradeTracker

UI mockup diagrams exported from http://draw.io/ on advfintech@gmail.com on Feb 9, 2018. More detailed specifications in KDocs and the [wiki](https://github.com/jimdc/TradeTracker/wiki).

![What the home screen will look like](/MainActivity.png "Main Activity")

### Model

![Add a stock to track](/AddStock.png "Add Stock")

**Stock** type stored in SQLite [table](https://github.com/jimdc/TradeTracker/blob/master/app/src/main/java/com/example/group69/alarm/stockbox.kt) [`TableVersion2`]

| Name          | Type          | Default  | 
| ------------- |:-------------:| --------:| 
| stockid       | Long          | 1337     |
| ticker        | String        | "BABA"   |
| target        | Double        | 4.20     |
| above         | Long          | 1        |
| phone         | Long          | 0        |
| crypto        | Long          | 0        |

### View

![Manage alarms for a stock](/AddAlarmExternal.png "Add Alarm External")

Uses a listview with a custom adapter.

### Controller

![Manage alarm type](/AddAlarmInternal.png "Add Alarm Internal")

Creates a service which starts up an AsyncTask thread to scrape HTML from NASDAQ and CryptoCompare.
