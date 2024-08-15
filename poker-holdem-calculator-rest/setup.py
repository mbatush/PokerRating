from setuptools import setup, find_packages

version = '1.0.0'

setup(
    name="holdem_calc_rest",
    version=version,
    author="Victor Stefoglo",
    author_email="victor.stefoglo@gmail.com",
    description="Holdem calculator REST",
    package_dir={'': 'src'},
    packages=find_packages("src"),
    url="https://github.com/stefvic",
    include_package_data=True,
    python_requires='>=3.10'
)
