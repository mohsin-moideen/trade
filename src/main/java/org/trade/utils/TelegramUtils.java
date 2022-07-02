package org.trade.utils;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;

public class TelegramUtils {
	static TelegramBot bot = new TelegramBot("5479641424:AAGurjJkzmfeBwKOR_KHLquyq4KeSSN6DaQ");
	public static boolean isDisabled = false;
	private static final Logger log = LogManager.getLogger(TelegramUtils.class);

	// Sends message to Fx trade updates group
	public static void sendMessage(String message) {
		sendMessage(message, "-676829240");
	}

	public static void sendMessage(String message, String chatId) {
		SendMessage request = new SendMessage(chatId, message).parseMode(ParseMode.HTML);
		Thread.currentThread().setName("TelegramUtils");
		if (isDisabled) {
			log.info("Telegram messaging disabled.");
			log.info("Message to sent -> " + message);
			return;
		}
		// async
		bot.execute(request, new Callback<SendMessage, SendResponse>() {

			@Override
			public void onResponse(SendMessage request, SendResponse response) {
				Thread.currentThread().setName("TelegramUtils");
				log.info("Telegram message sent -> " + JsonUtils.getString(response));
			}

			@Override
			public void onFailure(SendMessage request, IOException e) {
				Thread.currentThread().setName("TelegramUtils");
				log.error("Failed to send telegram message", e);

			}
		});
	}

	public static void setUpdatesListener(UpdatesListener listener) {
		bot.setUpdatesListener(listener);
	}

	public static void main(String[] args) throws InterruptedException {

		// sendMessage("message");

		// async
		bot.setUpdatesListener(new UpdatesListener() {
			@Override
			public int process(List<Update> updates) {

				System.out.println(updates);
				System.out.println(isDisabled);
				return UpdatesListener.CONFIRMED_UPDATES_ALL;
			}
		});
		while (true) {
			Thread.sleep(1000);
		}
	}
}
