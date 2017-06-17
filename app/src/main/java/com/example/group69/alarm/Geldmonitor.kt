package com.example.group69.alarm

import java.util.*
import java.sql.*
import javax.mail.*

class Geldmonitor : Runnable {
    init {
        Thread(Geldmonitor()).start()
        stub()
    }

    override fun run() {

        while (true) {
            try {

                println("thread start")
                //data.clear(); clears data from arraylist efficiently

                val properties = Properties()

                properties.put("mail.pop3.host", "pop.gmail.com")
                properties.put("mail.pop3.port", "995")
                properties.put("mail.pop3.starttls.enable", "true")

                val emailSession = Session.getDefaultInstance(properties)

                //create the POP3 store object and connect with the pop server
                val store = emailSession.getStore("pop3s")

                store.connect("imap.gmail.com", "mangomailbot@gmail.com",
                        "1234")

                //create the folder object and open it
                val emailFolder = store.getFolder("INBOX")
                emailFolder.open(Folder.READ_ONLY)

                // retrieve the messages from the folder in an array and print it
                val messages = emailFolder.messages
                println("messages.length---" + messages.size)
                var a: String
                val n = messages.size - 1
                var i = n
                while (i > n - 5) {
                    if (i == -1)
                    //reasoning for this is because javamail api sucks
                        break
                    val message = messages[i]
                    a = message.content.toString()

                    //	System.out.println("Email Number " + (i + 1));
                    //	System.out.println("Subject: " + message.getSubject());
                    //	System.out.println("From: " + message.getFrom()[0]);
                    println("Text: " + a)
                    println("Time: " + message.sentDate.time)

                    if (a[0] == '.') { //it's guaranteed to not be that 1 message
                        //that wont be deleted. all the msgs it checks it internally marks as checked except for the first one
                        data.add(a)
                    }
                    i--

                }

                //close the store and folder objects
                emailFolder.close(false)
                store.close()
                Thread.sleep(10000)
                if (sleep) {
                    Kontaktieren.send("sleeping for $sleepMins minutes")
                    println("sleeping for $sleepMins minutes in run thread")
                    Thread.sleep((sleepMins * 60000).toLong())
                }
                sleep = false
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

    }

    companion object {
        internal var num = 0 //just for testing threads
        internal var data = ArrayList<String>()
        internal var callCount = 0
        internal var sleep = false
        internal var sleepMins = 0

        fun stub() {
            try {
                val url = "jdbc:mysql://localhost:3306/databases?autoReconnect=true&useSSL=false"
                val conn = DriverManager.getConnection(url, "root", "9)") //password for the

                val stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)

                // if there happens to be nothing in the database keep looping, starting HERE

                val rs = stmt.executeQuery("select * from stocks;")

                //first we wanna query database to see if any actions need to be taken
                //then we wanna check if there was any commands sent to add or delete shit from the database
                var qTicker: String
                var qPrice: Double
                var qAB: String //above/below
                var qCall = "n"
                var print = false
                var result: Int //result of deletion
                //int numRows = 0;
                while (rs.next()) {
                    println(rs.getString("ticker"))
                    //	numRows++;
                }
                while (true) {
                    println(data)
                    if (data.size != 0) {
                        for (i in data.indices) {
                            val data2 = data[i].substring(1, data[i].length - 2)
                            println("data wasnt 0")
                            val queries = StringTokenizer(data2)
                            if (!queries.hasMoreElements()) {
                                Kontaktieren.send("You fucked up the syntax. Example: .a abc 2 b n == *period ticker add/delete price above/belowthat call?")
                            }

                            val choice = queries.nextToken()

                            if (choice == "a") {
                                println("adding...")

                                qTicker = queries.nextToken()
                                qPrice = java.lang.Double.parseDouble(queries.nextToken())
                                qAB = queries.nextToken()
                                qCall = queries.nextToken()

                                rs.moveToInsertRow()
                                rs.updateString("ticker", qTicker)
                                rs.updateDouble("price", qPrice)
                                rs.updateString("AB", qAB)
                                rs.updateString("phone", qCall)
                                rs.insertRow()
                                println("adding done")
                                Kontaktieren.send("added")
                            } else if (choice == "d") { //delete uses prepared stmts, add does not
                                println("deleting...")
                                qTicker = queries.nextToken()
                                if (queries.hasMoreElements()) { //case where a price is specified to delete
                                    qPrice = java.lang.Double.parseDouble(queries.nextToken())
                                    val sql = "Delete from stocks where ticker = ? and price = ?"
                                    val pst = conn.prepareStatement(sql)
                                    pst.setString(1, qTicker)
                                    pst.setDouble(2, qPrice)
                                    result = pst.executeUpdate()
                                    if (result != 1) {
                                        println("error deleting with price")
                                        Kontaktieren.send("error deleting with price")
                                    }
                                } else {
                                    val sql = "delete from stocks where ticker = ?"

                                    val pst = conn.prepareStatement(sql)
                                    pst.setString(1, qTicker) //number corresponds with which # ? in prepstat
                                    result = pst.executeUpdate()
                                    if (result != 1) {
                                        println("error deleting")
                                        Kontaktieren.send("error deleting")
                                    }
                                }
                                if (result == 1) {
                                    println("deleting done")
                                    Kontaktieren.send("deleted")
                                }

                            } //end delete
                            else if (choice == "p") {
                                print = true
                            } else if (choice == "s") {
                                sleep = true
                                sleepMins = Integer.parseInt(queries.nextToken())
                            } else if (choice == "e") {
                                Kontaktieren.send("program ended")
                                println("ending program")
                                System.exit(0) //ends program
                            } else {
                                Kontaktieren.send("listen mate ya fucked up the syntax ya hear?")
                            }
                        }//end for loop

                    }//end data != 0
                    data.clear()
                    //now we check to see if any of the databases figures indicate a reason to send a message

                    var currPrice: Double
                    var call: String

                    rs.absolute(0)//set pointer to begining of list

                    while (rs.next() == true) {
                        call = rs.getString("phone")
                        currPrice = SyntaxAnalysierer.priceOf(rs.getString("ticker"))
                        if (rs.getString("AB") == "b") {
                            if (currPrice <= java.lang.Double.parseDouble(rs.getString("price"))) {
                                Kontaktieren.send(rs.getString("ticker") + " (" + rs.getString("ticker") + ") has fallen to " + currPrice)
                                if (call == "y") {
                                    Kontaktieren.phoneCall(callCount % 2)
                                    callCount++
                                }
                                rs.deleteRow()
                            }
                        } else if (rs.getString("AB") == "a") {
                            if (currPrice >= java.lang.Double.parseDouble(rs.getString("price"))) {
                                Kontaktieren.send(rs.getString("ticker") + " (" + rs.getString("ticker") + ") has risen to " + currPrice)
                                if (call == "y") {
                                    Kontaktieren.phoneCall(callCount % 2)
                                    callCount++
                                }
                                rs.deleteRow()
                            }

                        }
                    }

                    rs.absolute(0)

                    if (print) { //send important data from table with current prices to phone
                        var p = ""
                        while (rs.next()) {
                            p = p + rs.getString("ticker") + " " + rs.getString("AB") + " " + rs.getString("phone") + " " +
                                    rs.getString("price") + " now: " + SyntaxAnalysierer.priceOf(rs.getString("ticker")) + "\r\n"
                        }
                        Kontaktieren.send(p)
                        rs.absolute(0)
                        print = false
                    }

                    Thread.sleep(6000) //change this to a bigger interval later to save processing power
                    if (sleep) {
                        println("sleeping for $sleepMins minutes in main thread")
                        Thread.sleep((sleepMins * 60000).toLong())
                    }
                    sleep = false //the other thread should definitly have had enough time to go to sleep, both will set to false
                } //end while true

            } catch (e: NoSuchElementException) {
                e.printStackTrace()
                Kontaktieren.send("restarting main thread; NSEe")
                try {
                    Thread.sleep(60000)
                } catch (e1: InterruptedException) {
                    e1.printStackTrace()
                }

                stub()

            } catch (e: Exception) {
                e.printStackTrace()
                Kontaktieren.send("restarting main thread")
                try {
                    Thread.sleep(60000)
                } catch (e1: InterruptedException) {
                    e1.printStackTrace()
                }

                Kontaktieren.send("restarting main thread")
                stub()
            }

        }
    }
}
