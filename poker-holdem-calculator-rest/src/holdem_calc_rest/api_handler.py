import os
import timeit
import typing

DEFAULT_SIMULATIONS = int(os.getenv('WIN_DEFAULT_SIMULATIONS', "100000"))
DEFAULT_SHOWDOWN_SIMULATIONS = int(os.getenv('SHOWDOWN_DEFAULT_SIMULATIONS', "250"))

if 'LOCAL_RUN' in os.environ:
    from models import WinPercentageRequest, PlayerWinPercentage, WinPercentageResponse, ShowdownPercentageRequest, \
        ShowdownPercentageResponse, HandRank
    from holdem_functions import generate_deck, Card, generate_hole_cards, find_winner, hand_rankings, \
        generate_random_boards, generate_exhaustive_boards, find_winning_percentage
else:
    from .models import WinPercentageRequest, PlayerWinPercentage, WinPercentageResponse, ShowdownPercentageRequest, \
        ShowdownPercentageResponse, HandRank
    from .holdem_functions import generate_deck, Card, generate_hole_cards, find_winner, hand_rankings, \
        generate_random_boards, generate_exhaustive_boards, find_winning_percentage


def create_hole_cards(raw_hole_cards):
    hole_cards, current_hole_cards = [], []
    for hole_card in raw_hole_cards:
        if hole_card != "?":
            current_card = Card(hole_card)
            current_hole_cards.append(current_card)
        else:
            current_hole_cards.append(None)
        if len(current_hole_cards) == 2:
            hole_cards.append((current_hole_cards[0], current_hole_cards[1]))
            current_hole_cards = []
    return tuple(hole_cards)


def create_cards(card_strings: typing.List[str]) -> typing.List[Card] | None:
    if not card_strings:
        return None
    return [Card(arg) for arg in card_strings]


def float_to_percentage(value: float, scale: int) -> float:
    return round(value * 100, scale)


def run_simulation(hole_cards, num, exact, given_board, deck):
    num_players = len(hole_cards)
    # Create results data structures which track results of comparisons
    # 1) result_histograms: a list for each player that shows the number of
    #    times each type of poker hand (e.g. flush, straight) was gotten
    # 2) winner_list: number of times each player wins the given round
    # 3) result_list: list of the best possible poker hand for each pair of
    #    hole cards for a given board
    result_histograms, winner_list = [], [0] * (num_players + 1)
    for _ in range(num_players):
        result_histograms.append([0] * len(hand_rankings))
    # Choose whether we're running a Monte Carlo or exhaustive simulation
    board_length = 0 if given_board is None else len(given_board)
    # When a board is given, exact calculation is much faster than Monte Carlo
    # simulation, so default to exact if a board is given
    if exact or given_board is not None:
        generate_boards = generate_exhaustive_boards
    else:
        generate_boards = generate_random_boards
    if (None, None) in hole_cards:
        hole_cards_list = list(hole_cards)
        unknown_index = hole_cards.index((None, None))
        for filler_hole_cards in generate_hole_cards(deck):
            hole_cards_list[unknown_index] = filler_hole_cards
            deck_list = list(deck)
            deck_list.remove(filler_hole_cards[0])
            deck_list.remove(filler_hole_cards[1])
            find_winner(generate_boards, tuple(deck_list),
                        tuple(hole_cards_list), num,
                        board_length, given_board, winner_list,
                        result_histograms)
    else:
        find_winner(generate_boards, deck, hole_cards, num,
                    board_length, given_board, winner_list,
                    result_histograms)

    return find_winning_percentage(winner_list), result_histograms


def calculate_win_percentage(win_percentage_request: WinPercentageRequest) -> WinPercentageResponse:
    start = timeit.default_timer()
    player_cards = []
    for player_card in win_percentage_request.players:
        player_cards = player_cards + player_card.split("|")
    hole_cards = create_hole_cards(player_cards)
    board = create_cards(win_percentage_request.board)
    board_and_exclude_cards = create_cards(win_percentage_request.board + win_percentage_request.excludes)
    deck = generate_deck(hole_cards, board_and_exclude_cards)
    exact = win_percentage_request.exact
    # [0.012684989429175475, 0.8562367864693446, 0.13107822410147993] - first element is ties percentage
    simulations = win_percentage_request.simulations \
        if win_percentage_request.simulations and not exact else DEFAULT_SIMULATIONS
    winner_result, result_histogram = run_simulation(hole_cards, simulations, exact, board, deck)

    scale = win_percentage_request.scale
    players_percentage = []
    for i in range(len(win_percentage_request.players)):
        rank_hand = None
        if board is not None and len(board) == 5:
            rank = result_histogram[i].index(1)
            rank_name = hand_rankings[rank]
            rank_hand = HandRank(rank_name, rank)
        players_percentage.append(
            PlayerWinPercentage(win_percentage_request.players[i], float_to_percentage(winner_result[i + 1], scale),
                                rank_hand))
    stop = timeit.default_timer()
    return WinPercentageResponse(players_percentage, float_to_percentage(winner_result[0], scale),
                                 round(stop - start, 6))


def showdown_run(hole_cards, num, exact, given_board, deck):
    num_players = 2
    # Choose whether we're running a Monte Carlo or exhaustive simulation
    board_length = len(given_board)

    # When a board has more than 3 cards, exact calculation is much faster than Monte Carlo
    # simulation, so default to exact if a board has more than 3 cards
    if exact or board_length > 3:
        generate_boards = generate_exhaustive_boards
    else:
        generate_boards = generate_random_boards

    hole_cards_list = [hole_cards[0], hole_cards[0]]
    total_simulated = 0
    total_player_one_win_greater_then_fifty = 0
    for filler_hole_cards in generate_hole_cards(deck):
        total_simulated += 1
        hole_cards_list[1] = filler_hole_cards
        deck_list = list(deck)
        deck_list.remove(filler_hole_cards[0])
        deck_list.remove(filler_hole_cards[1])

        # Create results data structures which track results of comparisons
        # 1) result_histograms: a list for each player that shows the number of
        #    times each type of poker hand (e.g. flush, straight) was gotten
        # 2) winner_list: number of times each player wins the given round
        # 3) result_list: list of the best possible poker hand for each pair of
        #    hole cards for a given board
        result_histograms, winner_list = [], [0] * (num_players + 1)
        for _ in range(num_players):
            result_histograms.append([0] * len(hand_rankings))
        find_winner(generate_boards, tuple(deck_list),
                    tuple(hole_cards_list), num,
                    board_length, given_board, winner_list,
                    result_histograms)
        result = find_winning_percentage(winner_list)
        player_one_win_percentage = float_to_percentage(result[1], 1)
        if player_one_win_percentage > 50.0:
            total_player_one_win_greater_then_fifty += 1

    return total_player_one_win_greater_then_fifty / total_simulated


def calculate_showdown_percentage(showdown_percentage_request: ShowdownPercentageRequest) -> ShowdownPercentageResponse:
    start = timeit.default_timer()
    exact = showdown_percentage_request.exact
    scale = showdown_percentage_request.scale
    hole_cards = create_hole_cards(showdown_percentage_request.player)
    board = create_cards(showdown_percentage_request.board)
    simulations = showdown_percentage_request.simulations \
        if showdown_percentage_request.simulations and not exact else DEFAULT_SHOWDOWN_SIMULATIONS
    deck = generate_deck(hole_cards, board)

    showdown_value = showdown_run(hole_cards, simulations, exact, board, deck)
    stop = timeit.default_timer()
    return ShowdownPercentageResponse(float_to_percentage(showdown_value, scale), round(stop - start, 6))
