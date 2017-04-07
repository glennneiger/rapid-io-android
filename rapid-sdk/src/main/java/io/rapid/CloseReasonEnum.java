package io.rapid;

enum CloseReasonEnum {
		UNKNOWN(Integer.MAX_VALUE), INTERNET_CONNECTION_LOST(1006), NO_INTERNET_CONNECTION(-1), CLOSED_MANUALLY(1000);

		private int mCode;


		CloseReasonEnum(int code) {
			mCode = code;
		}


		static CloseReasonEnum get(int code) {
			for(CloseReasonEnum item : CloseReasonEnum.values()) {
				if(item.getCode() == code) {
					return item;
				}
			}
			return UNKNOWN;
		}


		public int getCode() {
			return mCode;
		}

	}