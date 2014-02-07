package com.playtech.gamesimulator.service;


import com.playtech.wallet.domain.messages.WalletChangeMessage;
import com.playtech.wallet.domain.messages.WalletChangeResult;
import com.playtech.wallet.domain.messages.WalletChangeResultStatus;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

public class WalletServiceJsonClient implements WalletServiceClient {

    public WalletServiceJsonClient() {
        this("http://localhost:8080/wallet/wallet");
    }

    public WalletServiceJsonClient(String baseURI) {
        this.baseURI = baseURI;
        //set json support for rest restTemplate
        List<HttpMessageConverter<?>> list = new ArrayList<HttpMessageConverter<?>>();
        list.add(new MappingJackson2HttpMessageConverter());
        restTemplate.setMessageConverters(list);
    }

    private String baseURI;

    //use rest restTemplate to send message, it is thread safe
    RestTemplate restTemplate = new RestTemplate();


    @Override
    public WalletChangeResult sendMessage(WalletChangeMessage message) throws WalletServiceException {
        try {

            WalletChangeResult result = restTemplate.postForObject(baseURI, message, WalletChangeResult.class);
            handleWalletErrors(message, result);
            return result;

        } catch (RestClientException e) {
            throw new WalletServiceException("RestTemplate error when connecting to service (data WAS NOT passed to service)", e);
        }
    }

    private void handleWalletErrors(WalletChangeMessage message, WalletChangeResult result) throws WalletServiceException {

        if (result.getErrorCode().equals(WalletChangeResultStatus.NO_SUCH_PLAYER)) {
            throw new WalletServiceException("User does not exist: " + message.getUsername());
        };
        if (result.getErrorCode().equals(WalletChangeResultStatus.PLAYER_BALANCE_LESS_THAN_ZERO)) {
            throw new WalletServiceException(
                    String.format("Players %s balanceChange is to much: %s out of %s",
                                    message.getUsername(),
                                    result.getBalanceChange(),
                                    result.getTotalBalance()
                    )
            );
        }

    }


}
