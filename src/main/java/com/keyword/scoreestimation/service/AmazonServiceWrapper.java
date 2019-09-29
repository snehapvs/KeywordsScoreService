package com.keyword.scoreestimation.service;

import com.keyword.scoreestimation.controller.KeywordSearchController;
import com.keyword.scoreestimation.exception.BadRequestException;
import com.keyword.scoreestimation.exception.ExternalApiException;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class AmazonServiceWrapper {

	private static final Logger log = getLogger(KeywordSearchController.class);

	@Autowired
	private RestTemplate restTemplate;

	private final String AUTO_COMPLETE_API = "https://completion.amazon.com/search/complete?method=completion&mkt=1&search-alias=aps&q=";

	public List<String> getKeyWordData(String index) {
		try {
			log.info("Fetching keywords for prefix " + index);
			JSONArray responseArray = new JSONArray(
					restTemplate.getForObject(AUTO_COMPLETE_API.concat(index), String.class)
			).getJSONArray(1);
			return IntStream.range(0, responseArray.length())
					.mapToObj(responseArray::getString).collect(Collectors.toList());
		} catch (HttpStatusCodeException httpException) {
			if (httpException.getStatusCode().is4xxClientError()) {
				throw new BadRequestException(httpException.getStatusCode(), httpException.getResponseBodyAsString());
			} else {
                throw new ExternalApiException("Failed calling the Auto Complete api", httpException.getResponseBodyAsString());
			}
		}
	}

}
