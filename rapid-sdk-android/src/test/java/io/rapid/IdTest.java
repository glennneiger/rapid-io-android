package io.rapid;


import org.junit.Test;

import java.util.UUID;

import io.rapid.base.BaseTest;
import io.rapid.utility.UUIDUtility;

import static org.junit.Assert.assertEquals;


public class IdTest extends BaseTest {

	@Test
	public void testUUIDShortening() {
		assertEquals("_ga8VFwtQMCojiFoprnzew", UUIDUtility.base64(UUID.fromString("FA06BC54-5C2D-40C0-A88E-2168A6B9F37B")));
		assertEquals("-Rm9C7TOT3CeKtl7yB7nqQ", UUIDUtility.base64(UUID.fromString("FD19BD0B-B4CE-4F70-9E2A-D97BC81EE7A9")));
		assertEquals("BZ7DrZwCRGmMuFEx5rtl6A", UUIDUtility.base64(UUID.fromString("059EC3AD-9C02-4469-8CB8-5131E6BB65E8")));
		assertEquals("zA4-yXQdQEOVO3Ff1DZU4A", UUIDUtility.base64(UUID.fromString("CC0E3FC9-741D-4043-953B-715FD43654E0")));
	}
}
