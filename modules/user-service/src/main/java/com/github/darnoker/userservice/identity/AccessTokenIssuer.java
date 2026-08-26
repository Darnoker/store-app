package com.github.darnoker.userservice.identity;

import com.github.darnoker.userservice.user.model.User;

public interface AccessTokenIssuer {
    String issue(User user);
}
