package io.rollout.publicapi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.rollout.publicapi.model.Application;
import io.rollout.publicapi.model.AuditLog;
import io.rollout.publicapi.model.Environment;
import io.rollout.publicapi.model.Flag;
import io.rollout.publicapi.model.TargetGroup;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.httpclient.HttpException;

/**
 * Class for interacting with the Rollout Public API: https://docs.cloudbees.com/docs/cloudbees-feature-management-rest-api
 */
public class PublicApi {
    private static final String API_URL = "https://x-api.rollout.io/public-api";
    private static PublicApi instance;

    private ObjectMapper mapper;
    private final OkHttpClient client;

    /**
     * Creates a ConfigurationFetcher with the default {@link OkHttpClient}
     */
    public PublicApi() {
        this(new OkHttpClient.Builder().build());
    }

    public PublicApi(OkHttpClient client) {
        this.client = client;
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static PublicApi getInstance() {
        if (instance == null) {
            instance = new PublicApi();
        }

        return instance;
    }

    private <T> T get(HttpUrl url, String accessToken, TypeReference<T> typeReference) throws IOException {
        Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + accessToken)
                .get()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            return mapper.readValue(response.body().string(), typeReference);
        } else {
            throw new HttpException(response.body().string());
        }
    }

    public List<Application> listApplications(String accessToken) throws IOException {
        return get(HttpUrl.parse(API_URL + "/applications"), accessToken, new TypeReference<List<Application>>(){});
    }

    public List<Environment> listEnvironments(String accessToken, String applicationId) throws IOException {
        return get(HttpUrl.parse(API_URL + "/applications/" + applicationId + "/environments"), accessToken, new TypeReference<List<Environment>>(){});
    }

    public List<Flag> getFlags(String accessToken, String applicationId, String environmentName) throws IOException {
        return get(HttpUrl.parse(API_URL + "/applications/" + applicationId + "/" + environmentName + "/flags"), accessToken, new TypeReference<List<Flag>>(){});
    }

    public List<TargetGroup> getTargetGroups(String accessToken, String applicationId) throws IOException {
        return get(HttpUrl.parse(API_URL + "/applications/" + applicationId + "/target-groups"), accessToken, new TypeReference<List<TargetGroup>>(){});
    }

    public List<AuditLog> getAuditLogs(String accessToken, String applicationId, String environmentName, Date startDate) throws IOException {
        // TODO. The public API automatically paginates the response (30 items max). 😢
        HttpUrl url = HttpUrl
                .parse(API_URL + "/applications/" + applicationId + "/" + environmentName + "/auditlogs")
                .newBuilder()
                .addQueryParameter("startDate", startDate.toInstant().toString())
                .build();
        return get(url, accessToken, new TypeReference<List<AuditLog>>(){});
    }
}
