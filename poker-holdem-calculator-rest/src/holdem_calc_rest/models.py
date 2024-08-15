import os
import re
import typing

import marshmallow as ma
from marshmallow.validate import Range, Length, Regexp
from werkzeug.exceptions import BadRequest

CARD_PATTERN = '[AKQJT98765432][scdh]'
PLAYER_CARDS_PATTERN = f'{CARD_PATTERN}\\|{CARD_PATTERN}'

if 'LOCAL_RUN' in os.environ:
    from utils import auto_str
else:
    from .utils import auto_str

card_regex = re.compile(CARD_PATTERN)
player_card_regex = re.compile(PLAYER_CARDS_PATTERN)


def is_card_valid(card):
    if card_regex.match(card) is None:
        return False
    return True


def are_player_cards_valid(player_cards):
    if player_card_regex.match(player_cards) is None:
        return False
    return True


def validate_player_cards(player_cards: typing.List[str]):
    if len(player_cards) < 2:
        _badRequest = BadRequest()
        _badRequest.description = f'Players must contains at least 2 player cards'
        raise _badRequest
    for player_card in player_cards:
        if player_card != "?|?" and not are_player_cards_valid(player_card):
            _badRequest = BadRequest()
            _badRequest.description = f'Players contains invalid value: {player_card}. ' \
                                      f'Valid value pattern is: {PLAYER_CARDS_PATTERN}'
            raise _badRequest
        if player_cards.count(player_card) != 1:
            _badRequest = BadRequest()
            _badRequest.description = f'Players contains duplicate value: {player_card}'
            raise _badRequest
    return player_cards


def validate_card_and_unique(cards: typing.List[str], prefix_message: str):
    for card in cards:
        if not is_card_valid(card):
            _badRequest = BadRequest()
            _badRequest.description = f'${prefix_message} contains invalid card: {card}'
            raise _badRequest
        if cards.count(card) != 1:
            _badRequest = BadRequest()
            _badRequest.description = f'${prefix_message} contains duplicate card: {card}'
            raise _badRequest
    return cards


def validate_board_cards(cards: typing.List[str]):
    cards_len = len(cards)
    if cards_len != 0 and (cards_len < 3 or cards_len > 5):
        _badRequest = BadRequest()
        _badRequest.description = 'Board must have a length of 3, 4, or 5'
        raise _badRequest
    validate_card_and_unique(cards, "Board")

    return cards


def validate_exclude_cards(cards: typing.List[str]):
    validate_card_and_unique(cards, "Excludes")

    return cards


def validate_cards_are_unique(cards: typing.List[str]):
    for card in cards:
        if card != "?" and cards.count(card) != 1:
            _badRequest = BadRequest()
            _badRequest.description = f'Payload contains duplicate card: {card}'
            raise _badRequest
    return cards


@auto_str
class WinPercentageRequest:
    def __init__(self, players: typing.List[str], board: typing.List[str] = [], excludes: typing.List[str] = [],
                 simulations: int = None, exact: bool = False, scale: int = 0):
        self.players = validate_player_cards(players)
        self.board = validate_board_cards(board)
        self.excludes = validate_exclude_cards(excludes)
        self.simulations = simulations
        self.exact = exact
        self.scale = scale
        _player_cards = []
        for player_card in players:
            _player_cards = _player_cards + player_card.split("|")
        validate_cards_are_unique(self.board + self.excludes + _player_cards)


@auto_str
class HandRank:
    def __init__(self, name: str, rank: int):
        self.name = name
        self.rank = rank


@auto_str
class PlayerWinPercentage:
    def __init__(self, cards: str, win_percentage: float, hand_rank: HandRank = None):
        self.cards = cards
        self.winPercentage = win_percentage
        self.handRank = hand_rank


@auto_str
class WinPercentageResponse:
    def __init__(self, player_win_percentages: typing.List[PlayerWinPercentage], ties_percentage: float,
                 operation_time: float = -1):
        self.players = player_win_percentages
        self.tiesPercentage = ties_percentage
        self.operationTime = operation_time


class WinPercentageRequestSchema(ma.Schema):
    players = ma.fields.List(ma.fields.Str, required=True)
    board = ma.fields.List(ma.fields.Str, required=False)
    excludes = ma.fields.List(ma.fields.Str, required=False)
    simulations = ma.fields.Int(required=False, validate=[Range(min=1, error="Value must be greater then 0")])
    exact = ma.fields.Bool(required=False)
    scale = ma.fields.Int(required=False, validate=[Range(min=0, error="Value must be positive")])


class HandRankSchema(ma.Schema):
    name = ma.fields.Str()
    rank = ma.fields.Int()


class PlayerWinPercentageSchema(ma.Schema):
    cards = ma.fields.Str()
    winPercentage = ma.fields.Float()
    handRank = ma.fields.Nested(HandRankSchema)


class WinPercentageResponseSchema(ma.Schema):
    players = ma.fields.List(ma.fields.Nested(PlayerWinPercentageSchema))
    tiesPercentage = ma.fields.Float()
    operationTime = ma.fields.Float()


@auto_str
class ShowdownPercentageRequest:
    def __init__(self, player: typing.List[str], board: typing.List[str],
                 simulations: int = None, exact: bool = False, scale: int = 0):
        self.player = validate_card_and_unique(player, "Player")
        self.board = validate_board_cards(board)
        self.simulations = simulations
        self.exact = exact
        self.scale = scale
        validate_cards_are_unique(self.player + self.board)


@auto_str
class ShowdownPercentageResponse:
    def __init__(self, showdown_percentage: float, operation_time: float = -1):
        self.showdownPercentage = showdown_percentage
        self.operationTime = operation_time


class ShowdownPercentageRequestSchema(ma.Schema):
    player = ma.fields.List(ma.fields.Str(required=True,
                                          validate=[Regexp(regex=card_regex,
                                                           error="Player contains invalid card: {input}")]),
                            required=True, validate=[Length(equal=2)])
    board = ma.fields.List(ma.fields.Str(required=True,
                                         validate=[Regexp(regex=card_regex,
                                                          error="Board contains invalid card: {input}")]),
                           required=True, validate=[Length(min=3, max=5)])
    simulations = ma.fields.Int(required=False, validate=[Range(min=1, max=990)])
    exact = ma.fields.Bool(required=False)
    scale = ma.fields.Int(required=False, validate=[Range(min=0, error="Value must be positive")])


class ShowdownPercentageResponseSchema(ma.Schema):
    showdownPercentage = ma.fields.Float()
    operationTime = ma.fields.Float()
