package io.rapid;

class EntityOrderDetail {
	private String mProperty;
	private Sorting mSorting;


	EntityOrderDetail(String property, Sorting sorting) {
		mProperty = property;
		mSorting = sorting;
	}


	public Sorting getSorting() {
		return mSorting;
	}


	public void setSorting(Sorting sorting) {
		mSorting = sorting;
	}


	String getProperty() {
		return mProperty;
	}


	public void setProperty(String property) {
		mProperty = property;
	}
}
