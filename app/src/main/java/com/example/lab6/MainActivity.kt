package com.example.lab6

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.AsyncTask
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.xml.parsers.DocumentBuilderFactory

data class Metal(val code: String, val buy: String, val sell: String)

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val textView = TextView(this)
        FetchXmlData(textView).execute()
    }

    private inner class FetchXmlData(private val textView: TextView) : AsyncTask<Void, Void, List<Metal>>() {
        override fun doInBackground(vararg params: Void?): List<Metal> {
            val metalsList = mutableListOf<Metal>()

            val currentDate = Date()
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val Date1 = dateFormat.format(currentDate)

            val url = URL("https://www.cbr.ru/scripts/XML_metall.asp?date_req1=$Date1&date_req2=$Date1")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                val doc: Document = docBuilder.parse(inputStream)

                val recordList = doc.getElementsByTagName("Record")
                for (i in 0 until recordList.length) {
                    val element = recordList.item(i) as Element
                    val code = element.getAttribute("Code")
                    val buyElement = element.getElementsByTagName("Buy").item(0) as Element
                    val buyPrice = buyElement.textContent
                    val sellElement = element.getElementsByTagName("Sell").item(0) as Element
                    val sellPrice = sellElement.textContent
                    metalsList.add(Metal(code, buyPrice, sellPrice))
                }

                return metalsList
            }

            return emptyList()
        }

        override fun onPostExecute(result: List<Metal>) {
            val tableLayout = TableLayout(textView.context)

            val metalCodes = listOf("1", "2", "3", "4")

            val headerRow = TableRow(textView.context)
            val headerItem1 = TextView(textView.context)
            headerItem1.text = "Наименование"
            headerItem1.textSize = 20f
            val headerItem2 = TextView(textView.context)
            headerItem2.text = "Покупка"
            headerItem2.textSize = 20f
            val headerItem3 = TextView(textView.context)
            headerItem3.text = "Продажа"
            headerItem3.textSize = 20f

            headerRow.addView(headerItem1)
            headerRow.addView(headerItem2)
            headerRow.addView(headerItem3)
            tableLayout.addView(headerRow)

            for (code in metalCodes) {
                val metal = result.find { it.code == code }
                if (metal != null) {
                    val row = TableRow(textView.context)

                    val nameView = TextView(textView.context)
                    nameView.text = metal.code
                    nameView.textSize = 20f
                    val buyPriceView = TextView(textView.context)
                    buyPriceView.text = metal.buy
                    buyPriceView.textSize = 20f
                    val sellPriceView = TextView(textView.context)
                    sellPriceView.text = metal.sell
                    sellPriceView.textSize = 20f

                    row.addView(nameView)
                    row.addView(buyPriceView)
                    row.addView(sellPriceView)

                    tableLayout.addView(row)
                }
            }

            val layout = findViewById<TableLayout>(R.id.layoutId)
            layout.addView(tableLayout)
            for (i in 1 until tableLayout.childCount) {
                val row = tableLayout.getChildAt(i) as TableRow
                row.setOnClickListener {
                    val selectedMetalCode = (row.getChildAt(0) as TextView).text.toString()
                    val selectedMetal = result.find { it.code == selectedMetalCode }

                    if (selectedMetal != null) {
                        val widgetText =
                            "Выбран металл: ${selectedMetal.code}, Покупка: ${selectedMetal.buy}, Продажа: ${selectedMetal.sell}"
                        DataSingleton.getInstance().setString(widgetText)

                        val widgetProvider = ComponentName(this@MainActivity, MetalWidget::class.java)
                        val appWidgetManager = AppWidgetManager.getInstance(this@MainActivity)
                        val appWidgetIds = appWidgetManager.getAppWidgetIds(widgetProvider)
                        for (appWidgetId in appWidgetIds) {
                            MetalWidget.updateAppWidget(this@MainActivity, appWidgetManager, appWidgetId)
                        }
                        finish()
                    }
                }
            }
        }
    }
}