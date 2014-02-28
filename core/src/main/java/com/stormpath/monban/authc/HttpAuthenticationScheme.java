package com.stormpath.monban.authc;

public interface HttpAuthenticationScheme {

    String getName();

    //AuthenticationToken getAuthenticationToken(HttpServletRequest request, HttpServletResponse response) throws Exception;

}
