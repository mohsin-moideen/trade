package org.trade.utils.meta_api;

import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.trade.Constants;

import cloud.metaapi.sdk.clients.meta_api.SynchronizationListener;
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

			} catch (Exception e) {
				log.error("Failed to init metapi ", e);
			}
		}
		return api;
	}

	private static MetatraderAccount getMetaAccount() {
		if (account == null) {
			String accountId = Constants.META_API_ACCOUNT_ID;
			account = getMetaApi().getMetatraderAccountApi().getAccount(accountId).join();
		}
		return account;
	}

	public static MetaApiConnection getMetaApiConnection() {
		if (connection == null) {
			MetaApiConnection connection = getMetaAccount().connect().join();
			connection.waitSynchronized().join();
		}
		return connection;
	}

	public static void initMetaApi() {
		getMetaApiConnection();
	}

	public static void addSynchronizationListener(SynchronizationListener synchronizationListener) {
		getMetaApiConnection().addSynchronizationListener(synchronizationListener);
	}

	public static void main(String[] args) {

		MetatraderAccountInformation accountInfo = connection.getAccountInformation().join();
		System.out.println(accountInfo.balance);
		try {
			getMetaAccount().undeploy().get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
