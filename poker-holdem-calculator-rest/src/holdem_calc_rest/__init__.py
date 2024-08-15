import decimal
import os
import traceback
from flask_apispec import MethodResource, doc, marshal_with, use_kwargs, FlaskApiSpec
import flask.views
from flask import json, Response
from werkzeug.exceptions import HTTPException
from marshmallow.exceptions import ValidationError

if 'LOCAL_RUN' in os.environ:
    from models import WinPercentageRequestSchema, WinPercentageRequest, WinPercentageResponseSchema, \
        PlayerWinPercentage, WinPercentageResponse, ShowdownPercentageRequest, ShowdownPercentageResponse, \
        ShowdownPercentageRequestSchema, ShowdownPercentageResponseSchema
    from api_handler import calculate_win_percentage, calculate_showdown_percentage
else:
    from .models import WinPercentageRequestSchema, WinPercentageRequest, WinPercentageResponseSchema, \
        PlayerWinPercentage, WinPercentageResponse, ShowdownPercentageRequest, ShowdownPercentageResponse, \
        ShowdownPercentageRequestSchema, ShowdownPercentageResponseSchema
    from .api_handler import calculate_win_percentage, calculate_showdown_percentage

app = flask.Flask(__name__)
docs = FlaskApiSpec(app)


@app.errorhandler(Exception)
def exception_error_handler(e):
    """Return JSON instead of HTML for HTTP errors."""
    print("Exception error handler caught an error")
    traceback.print_exc()
    if isinstance(e, HTTPException):
        return handle_http_exception(e)

    return handle_generic_exception(e)


def handle_http_exception(e):
    """Return JSON instead of HTML for HTTP errors."""
    # start with the correct headers and status code from the error
    response = e.get_response()
    _description = e.description
    if hasattr(e, "exc") and isinstance(e.exc, ValidationError):
        _description = str(e.exc)

    # replace the body with JSON
    response.data = json.dumps({
        "code": e.code,
        "name": e.name,
        "description": _description,
        "stacktrace": traceback.format_exc()
    })
    response.content_type = "application/json"
    return response


def handle_generic_exception(e):
    response = Response(status=500)
    # replace the body with JSON
    response.data = json.dumps({
        "code": 500,
        "name": "Internal Server Error",
        "description": repr(e),
        "stacktrace": traceback.format_exc()
    })
    response.content_type = "application/json"
    return response


@doc(tags=['Poker Calculator'])
class HoldemCalcWinPercentageResource(MethodResource):

    @use_kwargs(WinPercentageRequestSchema)
    @marshal_with(WinPercentageResponseSchema, code=200)
    def post(self, **kwargs):
        win_percentage_request = WinPercentageRequest(**kwargs)
        result = calculate_win_percentage(win_percentage_request)
        return result


@doc(tags=['Poker Calculator'])
class HoldemCalcShowdownPercentageResource(MethodResource):

    @use_kwargs(ShowdownPercentageRequestSchema)
    @marshal_with(ShowdownPercentageResponseSchema, code=200)
    def post(self, **kwargs):
        showdown_percentage_request = ShowdownPercentageRequest(**kwargs)
        result = calculate_showdown_percentage(showdown_percentage_request)
        return result


@doc(tags=['Internal'])
class HealthResource(MethodResource):

    def get(self):
        return 'Ok'


app.add_url_rule('/health',
                 view_func=HealthResource.as_view('HealthResource'))
app.add_url_rule('/holdem/calc/win/percentage',
                 view_func=HoldemCalcWinPercentageResource.as_view('HoldemCalcWinPercentageResource'))
app.add_url_rule('/holdem/calc/showdown/percentage',
                 view_func=HoldemCalcShowdownPercentageResource.as_view('HoldemCalcShowdownPercentageResource'))

docs.register(HoldemCalcWinPercentageResource, endpoint='HoldemCalcWinPercentageResource')
docs.register(HoldemCalcShowdownPercentageResource, endpoint='HoldemCalcShowdownPercentageResource')
docs.register(HealthResource, endpoint='HealthResource')

if __name__ == '__main__':
    app.run()
