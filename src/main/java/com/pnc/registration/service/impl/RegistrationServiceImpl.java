package com.pnc.registration.service.impl;

import com.pnc.registration.exception.InputIsNullOrEmptyException;
import com.pnc.registration.exception.PasswordInvalidException;
import com.pnc.registration.exception.UserOutsideCanadaException;
import com.pnc.registration.model.RegistrationIpResponse;
import com.pnc.registration.model.RegistrationRequestBody;
import com.pnc.registration.model.RegistrationResponse;
import com.pnc.registration.service.RegistrationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class RegistrationServiceImpl implements RegistrationService {

    @Override
    public RegistrationResponse registration(RegistrationRequestBody registrationRequestBody) throws Exception {

        //validations
        if (registrationRequestBody.getUsername().equals("") || registrationRequestBody.getUsername() == null) {
            throw new InputIsNullOrEmptyException();
        }

        if (registrationRequestBody.getPassword().equals("") || registrationRequestBody.getPassword() == null) {
            throw new InputIsNullOrEmptyException();
        }

        if (registrationRequestBody.getIp().equals("") || registrationRequestBody.getIp() == null) {
            throw new InputIsNullOrEmptyException();
        }

        //password validations
        isAllPresent(registrationRequestBody.getPassword());

        //calling ip API
        RestTemplate restTemplate = new RestTemplate();
        String uri = "http://ip-api.com/json/";
        ResponseEntity<RegistrationIpResponse> responseIP = restTemplate.getForEntity(uri + registrationRequestBody.getIp(), RegistrationIpResponse.class);

        if (!responseIP.getBody().getCountry().equals("Canada")) {
            throw new UserOutsideCanadaException();
        }

        //constructing repsonse
        RegistrationResponse response = new RegistrationResponse();
        response.setCity(responseIP.getBody().getCity());
        UUID id = UUID.randomUUID();
        response.setID(id.toString());
        response.setWelcomeMessage("Welcome " + registrationRequestBody.getUsername());

        return response;
    }

    public void isAllPresent(String str) throws Exception {
        String regex = "^(?=.*[a-z])(?=."
                + "*[A-Z])(?=.*\\d)"
                + "(?=.*[_#$%.]).+$";

        Pattern p = Pattern.compile(regex);

        Matcher m = p.matcher(str);

        if (str.length() < 9) {
            throw new PasswordInvalidException();
        }

        if (m.matches()) {
            //do nothing
        }
        else {
            throw new Exception("Password does not contain at least 1 number, 1 capitalized letter, and 1 special character in this set: _#$%.");
        }
    }

}
