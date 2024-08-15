import os
import timeit

from holdem_functions import *

half_reverse_suit_index = ("s", "c")


def append_new_line(file_name, text_to_append):
    """Append given text as a new line at the end of file"""
    # Open the file in append & read mode ('a+')
    with open(file_name, "a+") as file_object:
        # Move read cursor to the start of file.
        file_object.seek(0)
        # If file is not empty then append '\n'
        data = file_object.read(100)
        if len(data) > 0:
            file_object.write("\n")
        # Append text at the end of file
        file_object.write(text_to_append)


def float_to_percentage(value: float, scale: int) -> float:
    return round(value * 100, scale)


def showdown_run(hole_cards, num, deck):
    num_players = 2
    # Choose whether we're running a Monte Carlo or exhaustive simulation
    board_length = 0
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
                    board_length, None, winner_list,
                    result_histograms)
        result = find_winning_percentage(winner_list)
        player_one_win_percentage = float_to_percentage(result[1], 1)
        if player_one_win_percentage > 50.0:
            total_player_one_win_greater_then_fifty += 1

    return total_player_one_win_greater_then_fifty / total_simulated


def main():
    calculated = []
    calculated_file = 'calculated_preflop_showdown.txt'
    with open(calculated_file) as f:
        for line in f:
            spited_line = line.split(' ')
            calculated.append((Card(spited_line[0]), Card(spited_line[1])))

    half_deck = []
    for suit in half_reverse_suit_index:
        for value in val_string:
            half_deck.append(Card(value + suit))

    for half_deck_card in generate_hole_cards(half_deck):
        if calculated.count(half_deck_card) > 0:
            continue
        start = timeit.default_timer()
        cards_ = [half_deck_card]
        deck = generate_deck(cards_, None)
        showdown_value = showdown_run(cards_, 20000, deck)
        stop = timeit.default_timer()
        total_time = round(stop - start, 6)
        showdown_percentage = float_to_percentage(showdown_value, 2)
        append_new_line(calculated_file, f'{half_deck_card[0]} {half_deck_card[1]} {showdown_percentage}')
        print(f'{half_deck_card}: {showdown_percentage}. Time {total_time}')
        print("Done")


if __name__ == '__main__':
    main()
