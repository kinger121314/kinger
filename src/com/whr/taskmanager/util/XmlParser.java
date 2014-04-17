package com.whr.taskmanager.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.whr.taskmanager.bean.VoiceMsg;

import android.util.Log;

public class XmlParser {
	private static final String TAG = "TaskManager";

	public static VoiceMsg parseNluResult(String xml) {

		VoiceMsg msg = new VoiceMsg();
		try {
			// DOM builder
			DocumentBuilder domBuilder = null;
			// DOM doc
			Document domDoc = null;

			// init DOM
			DocumentBuilderFactory domFact = DocumentBuilderFactory
					.newInstance();
			domBuilder = domFact.newDocumentBuilder();
			InputStream is = new ByteArrayInputStream(xml.getBytes());
			domDoc = domBuilder.parse(is);

			// 获取根节点
			Element root = (Element) domDoc.getDocumentElement();

			Element raw = (Element) root.getElementsByTagName("rawtext")
					.item(0);
			msg.voiceMsg = raw.getFirstChild().getNodeValue();
			Element object = (Element) root.getElementsByTagName("object")
					.item(0);
			Element datetime = (Element) object
					.getElementsByTagName("datetime").item(0);
			try {
				Element date = (Element) datetime.getElementsByTagName("date")
						.item(0);
				msg.date = date.getFirstChild().getNodeValue();
			} catch (Exception e) {
			}
			try {
				Element time = (Element) datetime.getElementsByTagName("time")
						.item(0);
				msg.time = time.getFirstChild().getNodeValue();
			} catch (Exception e) {
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		Log.d(TAG, "xml:" + xml);
		return msg;
	}
}
