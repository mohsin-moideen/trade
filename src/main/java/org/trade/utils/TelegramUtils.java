package org.trade.utils;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.trade.App;

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

	public static void setTelegramListener(final Map<String, App> apps) {
		final String STRATEGY = "STRATEGY";
		final String BOT = "BOT";
		final String PAUSE = "PAUSE";
		final String RESUME = "RESUME";

		setUpdatesListener(new UpdatesListener() {
			@Override
			public int process(List<Update> updates) {

				System.out.println(updates);
				for (Update update : updates) {
					String[] tokens = update.message().text().trim().split(" ");
					String auth = tokens[tokens.length - 1];
					String fromUser = update.message().from().username();
					String chatId = update.message().chat().id().toString();

					if (!isAuthValid(auth, fromUser)) {
						sendMessage("Invalid auth");
						sendMessage("Requested by -> " + update.message().from());
						sendMessage("Requested via chat -> " + update.message().chat());
						sendMessage("Requested message -> " + update.message().text());
						break;
					}
					if (tokens[0].equals(BOT)) {
						switch (tokens[1]) {
						case PAUSE: {
							for (App app : apps.values()) {
								app.pause();
							}
							sendMessage("Bot paused by " + fromUser);
							break;

						}
						case RESUME: {
							for (App app : apps.values()) {
								app.resume();
							}
							sendMessage("Bot resumed by " + fromUser);
							break;

						}
						default: {
							sendMessage("Invalid action " + tokens[1], chatId);
						}
						}
					} else if (tokens[0].equals(STRATEGY)) {
						App app = apps.get(tokens[1]);
						if (app == null) {
							sendMessage("Invalid strategy " + tokens[1], chatId);
						}
						switch (tokens[2]) {
						case PAUSE: {
							app.pause();
							sendMessage("Straegy " + tokens[1] + " paused by " + fromUser);
							break;
						}
						case RESUME: {
							app.resume();
							sendMessage("Straegy " + tokens[1] + " resumed by " + fromUser);
							break;
						}
						default: {
							sendMessage("Invalid action " + tokens[2], chatId);
						}
						}
					} else {
						sendMessage("Invalid action object " + tokens[0], chatId);

					}
				}
				return UpdatesListener.CONFIRMED_UPDATES_ALL;
			}

			private boolean isAuthValid(String auth, String fromUser) {
				try {
					int otp = Integer.parseInt(auth);
					return getValidUserIds().contains(fromUser) && GoogleAuthUtils.authorize(fromUser, otp);
				} catch (Exception e) {
					Thread.currentThread().setName("TelegramUtils");
					log.info("Missing otp");
				}
				return false;
			}

			private Set<String> getValidUserIds() {
				// TODO make this list configurable
				Set<String> validUsers = new HashSet<>();
				validUsers.add("mo9001");
				return validUsers;
			}
		});

	}
}
