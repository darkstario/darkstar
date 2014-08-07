package io.darkstar.ducksboard;

public class DucksboardPoster {

    /*
    private static final Logger log = LoggerFactory.getLogger(DucksboardPoster.class);
    private static final String BASE_URL = "https://push.ducksboard.com/v/";
    private static final String AUTHORIZATION = "Authorization";
    private static final Charset UTF8 = Charset.forName("UTF-8");

    private final RestTemplate restTemplate;
    private final HttpHeaders headers;

    public DucksboardPoster(String apiKey, RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        String basicSchemeValue = apiKey + ":unused";
        String authzHeaderValue = "Basic " + Base64.encodeToString(basicSchemeValue.getBytes(UTF8));

        this.headers = new HttpHeaders();
        headers.set(AUTHORIZATION, authzHeaderValue);
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    public void post(String label, String json) {
        final HttpEntity<String> entity = new HttpEntity<>(json, headers);

        String uri = BASE_URL + label;

        try {
            log.trace("Posting JSON to Ducksboard: URI: {}, json: {}", uri, json);
            ResponseEntity responseEntity = restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
            if (responseEntity.getStatusCode() != HttpStatus.OK) {
                throw new TransientDataAccessResourceException("Ducksboard post failed.  Response status: " +
                        responseEntity.getStatusCode()  + ", body: " + responseEntity.getBody());
            }
            log.trace("Ducksboard response: status: {}, body: {}", responseEntity.getStatusCode(), responseEntity.getBody());
        } catch (Exception e) {
            log.warn("Unable to post content to Ducksboard", e);
        }
    }
    */

}
