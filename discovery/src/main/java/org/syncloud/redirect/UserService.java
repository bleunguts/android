package org.syncloud.redirect;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.syncloud.model.RestMessage;
import org.syncloud.model.Result;

import java.util.ArrayList;
import java.util.List;

public class UserService {

    private String apiUrl;

    public UserService(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public Result<Boolean> getUser(String email, String password) {

        CloseableHttpClient http = HttpClients.createDefault();
        HttpGet get = new HttpGet(apiUrl +
                "/user/get?email=" + email +
                "&password=" + password);

        try {
            CloseableHttpResponse response = http.execute(get);

            int statusCode = response.getStatusLine().getStatusCode();
            response.close();
            if (statusCode == 200) {
                return Result.value(true);
            } else {
                ObjectMapper mapper = new ObjectMapper();
                RestMessage reply = mapper.readValue(response.getEntity().getContent(), RestMessage.class);
                return Result.error(reply.getMessage());
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    public Result<String> createUser(String email, String password, String domain) {

        CloseableHttpClient http = HttpClients.createDefault();
        HttpPost post = new HttpPost(apiUrl + "/user/create");

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("email", email));
        nvps.add(new BasicNameValuePair("password", password));
        nvps.add(new BasicNameValuePair("user_domain", domain));
        try {
            post.setEntity(new UrlEncodedFormEntity(nvps));
            CloseableHttpResponse response = http.execute(post);
            ObjectMapper mapper = new ObjectMapper();
            RestMessage reply = mapper.readValue(response.getEntity().getContent(), RestMessage.class);
            int statusCode = response.getStatusLine().getStatusCode();
            response.close();
            if (statusCode == 200) {
                return Result.value(reply.getMessage());
            } else {
                return Result.error(reply.getMessage());
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    public Result<Boolean> getOrCreate(String email, String password, String domain) {
        Result<Boolean> user = getUser(email, password);
        if (user.hasError()) {
            Result<String> create = createUser(email, password, domain);
            if (create.hasError())
                return Result.error(create.getError());
            user = getUser(email, password);
        }
        return user;
    }
}
