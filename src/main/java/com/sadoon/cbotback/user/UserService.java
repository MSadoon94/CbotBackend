package com.sadoon.cbotback.user;

import com.sadoon.cbotback.card.models.Card;
import com.sadoon.cbotback.exceptions.UserNotFoundException;
import com.sadoon.cbotback.user.models.User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserService {

    private UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public User getUser(String username) throws UserNotFoundException {
        User user = repo.getUserByUsername(username);
        if (user == null) {
            throw new UserNotFoundException(username);
        } else {
            return user;
        }
    }

    public void deleteAll() {
        repo.deleteAll();
    }

    public User save(User user) {
        return repo.save(user);
    }

    public void addCard(User user, Card card) {
        Map<String, Card> cards = user.getCards();
        cards.put(card.getCardName(), card);
        user.setCards(cards);
        repo.delete(user);
        repo.save(user);
    }
}
