import logging
import sys
import colorlog


def setup_logger(name: str = __name__) -> logging.Logger:
    logger = logging.getLogger(name)
    logger.setLevel(logging.INFO)

    # Console handler with color
    console_handler = colorlog.StreamHandler(sys.stdout)
    console_handler.setLevel(logging.INFO)
    
    # Color formatter
    color_formatter = colorlog.ColoredFormatter(
        "%(log_color)s%(asctime)s - %(name)s - %(levelname)s - %(message)s",
        log_colors={
            'DEBUG':    'cyan',
            'INFO':     'green',
            'WARNING': 'yellow',
            'ERROR':   'red',
            'CRITICAL': 'red,bg_white',
        }
    )
    console_handler.setFormatter(color_formatter)

    # Add handlers to logger
    logger.addHandler(console_handler)

    return logger

# Create a default logger instance
logger = setup_logger()