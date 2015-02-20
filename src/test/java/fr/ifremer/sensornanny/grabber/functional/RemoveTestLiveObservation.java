package fr.ifremer.sensornanny.grabber.functional;

import org.junit.Test;

import fr.ifremer.sensornanny.grabber.AbstractTest;

public class RemoveTestLiveObservation extends AbstractTest {

	private String[] TO_REMOVE = {
			"d8326d7e-815b-11e4-a0e2-5c260a184584",
			"c49a8f6a-5ffe-4922-b7eb-048770282e3c",
			"c2c44895-77ec-4708-90ec-0229e77ebc30",
			"512aaa81-1f39-420b-9e7e-c9588994508b",
			"78e2a984-8b71-4838-a771-3f0e99c132e7",
			"5e792eef-926c-40b8-9546-07992f883e71",
			"1c0e43a0-b606-456b-89f5-d6ceb253fc2a",
			"913468cf-5a9f-467c-9f38-37f4a36c77db",
			"4daf1fb2-8d1f-40b3-b30f-a8f0d1a1cc64",
			"745547f9-b080-4b06-8f3d-f7aa4297169b",
			"289f5870-6093-4476-a9f7-90473cea31a4",
			"326d812d-2fa7-4f21-9f55-2e7346d98a98",
			"43e82c45-4fcf-442b-a23e-6a6b721d8486",
			"b822e0e8-d610-45f3-99c8-856416ed4f64",
			"56c546e1-c18a-4206-9c6b-2e2013cb9f88",
			"88a7b705-ae3a-4919-bca1-7eab9be8bf81",
	};
	
	@Test
	public void test() throws Exception {
		
		for (String each : TO_REMOVE) {
			generatedUUID = each;
			cleanupObservation();
		}
		
		generatedUUID = null; // No automatic deletion
	}

}
