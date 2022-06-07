package org.trade.config;

public class Constants {

	public static String META_API_ACCOUNT_ID = "911d4ed9-f215-4cd9-8e51-b601a197acc6";
	public static String META_API_API_KEY = "eyJhbGciOiJSUzUxMiIsInR5cCI6IkpXVCJ9.eyJfaWQiOiJmNzU2ZjhiMzEzNmZjNzdiMWVjZDNkOGM1MzUxNTM1OCIsInBlc"
			+ "m1pc3Npb25zIjpbXSwidG9rZW5JZCI6IjIwMjEwMjEzIiwiaWF0IjoxNjUxOTQzMDAwLCJyZWFsVXNlcklkIjoiZjc1NmY4YjMxMzZmYzc3YjFlY2QzZDhjNTM1MTUzNT"
			+ "gifQ.BnWcUuV1R7f_RRZbR1_8_OrFxhe8BTMuWnfhiSCAZgaKYkmSZztHBjlHk4SZK46EDLQeIkeTgCOhs8NKToyQWUhC-RUCF7mcGjatOeyY28bwnc72C4DrOzdRUOb9M"
			+ "qK22OFIfcCwagTu_2u1gg7aaERV3P151qfRrem6lOJ9K9rxwknWnp_GXXW_AUCouJs_5F0LbMgjFFSYSvcvUf8JQro5E7-48UH2NF0NBq9flUeNL6nPrPULYhy_yMwovdpS"
			+ "eNNO5BelT-_jVVu-zUCKUU5gZXvQZ_ap3FuY5bUTTQaMxoVQ6kNVLhrB8pJZvxo529ZqLvg8i96IpUz7pTnxtYEn-Fb6s5dePQpYjqggRLypCiIdbCgsjgXLiSH9nV7t8Hw"
			+ "aO5MyfN8eHhZagfO71Gin16POHl7jiLtNWEVcaw6K2G_xZg9QoyBJGjf-0Lzn9cliDm4kq6ffjlUkksF794cIBQsD0b5UY3oCv_sZ9Oz8CPt6SrMbiiAYdLWWeBJy3u2BJsRz"
			+ "Xz9tJ4uDHJgadn-WO7GefanH84QTfzsD0bHU3mWmr5LFG5e6l2Sm4xGJM71iEWO6Y7eHAaCI_2gaMueuM0i7exIWwb6lbLqq-_KCA95X7nCZpQiSE7AHxSsV6Qom-821Nbzp"
			+ "bny72Ea98wyKr_AnPZ8HI_rBhaxbTOo";
	public static String META_API_BASE_PATH_SERVER = "https://mt-client-api-v1.london.agiliumtrade.ai";
	public static String META_API_SERVER_REGION = "london";
	public static String META_API_BASE_PATH_STATS = "https://metastats-api-v1.london.agiliumtrade.ai";
	public static String META_API_BASE_PATH_HISTORIC_DATA = "https://mt-market-data-client-api-v1.london.agiliumtrade.ai";

	public static String HISTORIC_DATA_ENDPOINT = META_API_BASE_PATH_HISTORIC_DATA + "/users/current/accounts/"
			+ META_API_ACCOUNT_ID + "/historical-market-data/symbols/{symbol}/timeframes/{timeframe}/candles";
	public static String CURRENT_CANDLE_ENDPOINT = META_API_BASE_PATH_SERVER + "/users/current/accounts/"
			+ META_API_ACCOUNT_ID + "/symbols/{symbol}/current-candles/{timeframe}";
	public static String TRADE_ENDPOINT = META_API_BASE_PATH_SERVER + "/users/current/accounts/" + META_API_ACCOUNT_ID
			+ "/trade";
}
