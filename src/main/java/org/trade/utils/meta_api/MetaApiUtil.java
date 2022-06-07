package org.trade.utils.meta_api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.trade.config.Constants;
import org.trade.utils.JsonUtils;

import cloud.metaapi.sdk.clients.meta_api.models.MetatraderAccountInformation;
import cloud.metaapi.sdk.meta_api.MetaApi;
import cloud.metaapi.sdk.meta_api.MetaApi.Options;
import cloud.metaapi.sdk.meta_api.MetaApiConnection;
import cloud.metaapi.sdk.meta_api.MetatraderAccount;

public class MetaApiUtil {
	private static MetaApi api;
	private static MetatraderAccount account;
	private static MetaApiConnection connection;

	private static final Logger log = LogManager.getLogger(MetaApiUtil.class);

	private static MetaApi getMetaApi() {
		if (api == null) {
			try {
				Options options = new Options();
				options.region = Constants.META_API_SERVER_REGION;
				api = new MetaApi(Constants.META_API_API_KEY, options);
				log.info("Successfully retrieved meta api");
			} catch (Exception e) {
				log.error("Failed to init metapi ", e);
			}
		}
		return api;
	}

	private static MetatraderAccount getMetaAccount() {
		if (account == null) {
			String accountId = Constants.META_API_ACCOUNT_ID;
			try {
				account = getMetaApi().getMetatraderAccountApi().getAccount(accountId).get();
				log.info("Successfully retrieved meta api account " + account.getId());
			} catch (Exception e) {
				log.error("Failed to get MetaApi connection", e);

			}
		}
		return account;
	}

	public static MetaApiConnection getMetaApiConnection() {
		if (connection == null) {
			try {
				connection = getMetaAccount().connect().get();
				connection.waitSynchronized().get();
				log.info("Successfully retrieved meta api account connection for" + account.getId());

			} catch (Exception e) {
				log.error("Failed to get MetaApi connection", e);
			}
		}
		return connection;
	}

	public static void initMetaApi() {
		getMetaApiConnection();
	}

	public static void main(String[] args) {
		initMetaApi();
		MetatraderAccountInformation accountInfo = getMetaApiConnection().getAccountInformation().join();
		System.out.println(accountInfo.balance);
		System.out.println(JsonUtils.getString(connection.getPositions().join()));

	}

}
