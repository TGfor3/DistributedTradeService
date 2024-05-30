FROM python:3.9-slim

WORKDIR /app

ADD . /app/

RUN pip install --trusted-host pypi.python.org -r requirements.txt
RUN pip install pika
RUN pip list

# Without this setting, Python never prints anything out.
ENV PYTHONUNBUFFERED=1

EXPOSE 5000

CMD ["python", "Trade_Service.py"]