package org.trade.utils;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;

public class TelegramUtils {
	static TelegramBot bot = new TelegramBot("5479641424:AAGurjJkzmfeBwKOR_KHLquyq4KeSSN6DaQ");
	public static boolean isDisabled = false;
	private static final Logger log = LogManager.getLogger(TelegramUtils.class);

	public static void sendMessage(String message) {
		SendMessage request = new SendMessage("-676829240", message).parseMode(ParseMode.HTML);
		if (isDisabled) {
			log.info("Telegram messaging disabled.");
			log.info("Message to sent -> " + message);
			return;
		}
		// async
		bot.execute(request, new Callback<SendMessage, SendResponse>() {
			@Override
			public void onResponse(SendMessage request, SendResponse response) {
				log.info("Telegram message sent -> " + JsonUtils.getString(response));
			}

			@Override
			public void onFailure(SendMessage request, IOException e) {
				log.error("Failed to send telegram message", e);

			}
		});
	}

	public static void main(String[] args) {
		sendMessage("message");
	}
}
