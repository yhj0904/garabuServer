package garabu.garabuServer.service;

import garabu.garabuServer.domain.Member;
import garabu.garabuServer.dto.*;
import garabu.garabuServer.repository.MemberJPARepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberJPARepository memberJPARepository;

    public CustomOAuth2UserService(MemberJPARepository memberJPARepository) {

        this.memberJPARepository = memberJPARepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        System.out.println(oAuth2User);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response = null;
        if (registrationId.equals("naver")) {

            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
        }
        else if (registrationId.equals("google")) {

            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        }
        else if (registrationId.equals("apple")) {

            oAuth2Response = new AppleResponse(oAuth2User.getAttributes());
        }
        else if (registrationId.equals("kakao")) {

            oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
        }
        else {

            return null;
        }
        String username = oAuth2Response.getProvider()+" "+oAuth2Response.getProviderId();
        String email = oAuth2Response.getEmail();
        String providerId = oAuth2Response.getProviderId();

        Optional<Member> optionalMember = memberJPARepository.findByProviderIdAndEmail(providerId, email);
        Member existData = optionalMember.orElse(null);

        //providerID PK

        /*String username = oAuth2Response.getEmail();  // 사용자의 이메일을 username으로 사용
        Member existData = memberJPARepository.findOneByEmail(username);*/

        if (existData == null) {

            Member userEntity = new Member();
            userEntity.setUsername(username);
            userEntity.setEmail(oAuth2Response.getEmail());
            userEntity.setName(oAuth2Response.getName());
            userEntity.setRole("ROLE_USER");
            userEntity.setProviderId(providerId);

            memberJPARepository.save(userEntity);

            UserDTO userDTO = new UserDTO();
            userDTO.setUsername(username);
            userDTO.setName(oAuth2Response.getName());
            userDTO.setEmail(oAuth2Response.getEmail());
            userDTO.setRole("ROLE_USER");
            userDTO.setProviderId(providerId);

            return new CustomOAuth2User(userDTO);
        }
        else {

            existData.setEmail(oAuth2Response.getEmail());
            existData.setUsername(username);

            memberJPARepository.save(existData);

            UserDTO userDTO = new UserDTO();
            userDTO.setUsername(existData.getUsername());
            userDTO.setEmail(oAuth2Response.getEmail());
            userDTO.setName(oAuth2Response.getName());
            userDTO.setRole(existData.getRole());
            userDTO.setProviderId(providerId);

            return new CustomOAuth2User(userDTO);
        }
    }
}
