package com.advent.group69.tradetracker

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.HashMap
import java.util.Properties

import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object Kontaktieren {
    /**
     * Either calls Nick or calls James by [performPostCall] to wheresmycellphone.com
     * @param[n] 1 calls Nick, 2 calls James
     */
    @Throws(InterruptedException::class)
    fun phoneCall(n: Int) {

        println("Option 1 or option 2?")
        //later on change this so it just cycles through each method (increments i and does %2 on i)

        if (n == 0) {

            val params = HashMap<String, String>()
            params.put("recipient", "7328500309") //Nick.
            params.put("noWhen", "0") //is actually "w-select freetextfield forminline"

            performPostCall("http://www.callmylostphone.com/", params)

        } else if (n == 1) {

            val params = HashMap<String, String>()
            params.put("iArea", "201")
            params.put("iNumb", "9899857") //James.
            params.put("iWhen", "0")

            performPostCall("http://wheresmycellphone.com/", params)
        }//this is for callmylostphone

    }

    /**
     * @param[params] Unformatted data to POST
     * @return String version of POST parameters
     * @sample performPostCall
     */
    @Throws(UnsupportedEncodingException::class)
    private fun getPostDataString(params: HashMap<String, String>): String {
        val result = StringBuilder()
        var first = true
        for ((key, value) in params) {
            if (first)
                first = false
            else
                result.append("&")

            result.append(URLEncoder.encode(key, "UTF-8"))
            result.append("=")
            result.append(URLEncoder.encode(value, "UTF-8"))
        }

        return result.toString()
    }

    /**
     * Performs a POST call with a 15 second timeout, UTF-8
     * @param[requestURL] The URL that takes the request
     * @param[postDataParams] The information to post
     * @return The server's response if successful, or ""
     */
    private fun performPostCall(requestURL: String,
                        postDataParams: HashMap<String, String>): String {

        val url: URL
        var response = ""
        try {
            url = URL(requestURL)

            val conn = url.openConnection() as HttpURLConnection
            conn.readTimeout = 15000
            conn.connectTimeout = 15000
            conn.requestMethod = "POST"
            conn.doInput = true
            conn.doOutput = true


            val os = conn.outputStream
            val writer = BufferedWriter(
                    OutputStreamWriter(os, "UTF-8"))
            writer.write(getPostDataString(postDataParams))

            writer.flush()
            writer.close()
            os.close()
            val responseCode = conn.responseCode

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val br = BufferedReader(InputStreamReader(conn.inputStream))
                var line: String? = br.readLine()
                while (line != null) {
                    response += line
                    line = br.readLine()
                }
            } else {
                response = ""

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return response
    }

    /**
     * Sends a text to Nick via "mangomailbox" and javax
     * @param[msg] The body of the text to send in the text
     */
    fun send(msg: String) {

        val username = "mangomailbot@gmail.com"
        val password = "qwerqwer1234"

        val props = Properties()
        props.put("mail.smtp.auth", "true")
        props.put("mail.smtp.starttls.enable", "true")
        props.put("mail.smtp.host", "smtp.gmail.com")
        props.put("mail.smtp.port", "587")


        val session = Session.getInstance(props,
                object : javax.mail.Authenticator() {
                    override fun getPasswordAuthentication(): javax.mail.PasswordAuthentication {
                        return javax.mail.PasswordAuthentication(username, password)
                    }
                })

        try {

            val message = MimeMessage(session)
            message.setFrom(InternetAddress(username))
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse("7328500309@vtext.com"))
            message.setText(msg)

            Transport.send(message)

            println("msg sent")

        } catch (e: MessagingException) {
            throw RuntimeException(e)
        }

    }
}
