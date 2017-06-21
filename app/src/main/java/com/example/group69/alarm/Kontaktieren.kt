package com.example.group69.alarm

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
    @Throws(InterruptedException::class)
    fun phoneCall(n: Int) {

        println("Option 1 or option 2?")
        //later on change this so it just cycles through each method (increments i and does %2 on i)

        if (n == 0) {

            val params = HashMap<String, String>()
            params.put("recipient", "7328500309") //Nick.
            params.put("noWhen", "0") //is actually "w-select freetextfield forminline"

            val response = performPostCall("http://www.callmylostphone.com/", params)

        } else if (n == 1) {

            val params = HashMap<String, String>()
            params.put("iArea", "201")
            params.put("iNumb", "9899857") //James.
            params.put("iWhen", "0")

            val response = performPostCall("http://wheresmycellphone.com/", params)
        }//this is for callmylostphone

    }

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

    fun performPostCall(requestURL: String,
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

    fun send(msg: String) {

        val username = "mangomailbot@gmail.com"
        val password = "1234"

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
                    InternetAddress.parse("7328500309@vtext.com")) //kevins phone lol: 8482193579@tmomail.net
            //message.setSubject("Testing Subject"); //check if this can be removed
            message.setText(msg)

            Transport.send(message)

            println("msg sent")

        } catch (e: MessagingException) {
            throw RuntimeException(e)
        }

    }
}
