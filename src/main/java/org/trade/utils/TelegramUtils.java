package org.trade.utils;

import java.io.IOException;

import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;

public class TelegramUtils {
	static TelegramBot bot = new TelegramBot("5479641424:AAGurjJkzmfeBwKOR_KHLquyq4KeSSN6DaQ");

	public static void sendMessage(String message) {
		SendMessage request = new SendMessage("-676829240", message).parseMode(ParseMode.HTML);
		// async
		bot.execute(request, new Callback<SendMessage, SendResponse>() {
			@Override
			public void onResponse(SendMessage request, SendResponse response) {
				System.out.println(JsonUtils.getString(response));
			}

			@Override
			public void onFailure(SendMessage request, IOException e) {
				System.out.println(JsonUtils.getString(e.getMessage()));

			}
		});
	}

	public static void main(String[] args) {
		sendMessage("message");
	}
}
