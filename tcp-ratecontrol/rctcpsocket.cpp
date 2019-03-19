/****************************************************************************
**
** Copyright (C) 2006 Trolltech AS. All rights reserved.
**
** This file is part of the documentation of Qt. It was originally
** published as part of Qt Quarterly.
**
** This file may be used under the terms of the GNU General Public License
** version 2.0 as published by the Free Software Foundation or under the
** terms of the Qt Commercial License Agreement. The respective license
** texts for these are provided with the open source and commercial
** editions of Qt.
**
** If you are unsure which license is appropriate for your use, please
** review the following information:
** http://www.trolltech.com/products/qt/licensing.html or contact the
** sales department at sales@trolltech.com.
**
** This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
** WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
**
****************************************************************************/

#include "rctcpsocket.h"

#include <QtCore/QDebug>
#include <QtCore/QTimer>

RcTcpSocket::RcTcpSocket(QObject *parent)
    : QTcpSocket(parent)
{
    connect(this, SIGNAL(readyRead()), this, SIGNAL(readyToTransfer()));
    connect(this, SIGNAL(connected()), this, SIGNAL(readyToTransfer()));
}

qint64 RcTcpSocket::writeToSocket(qint64 maxlen)
{
    qint64 bytesWritten = QTcpSocket::writeData(outgoing.data(), 
						qMin<qint64>(maxlen, outgoing.size()));
    if (bytesWritten <= 0)
	return bytesWritten;
    outgoing.remove(0, bytesWritten);
    return bytesWritten;
}

qint64 RcTcpSocket::readFromSocket(qint64 maxlen)
{
    int oldSize = incoming.size();
    incoming.resize(incoming.size() + maxlen);
    qint64 bytesRead = QTcpSocket::readData(incoming.data() + oldSize, maxlen);
    incoming.resize(bytesRead <= 0 ? oldSize : oldSize + bytesRead);
    if (bytesRead > 0)
	emit readyRead();
    return bytesRead;
}

qint64 RcTcpSocket::bytesAvailable() const
{
    if (state() != ConnectedState) {
	QByteArray buffer;
	buffer.resize(QTcpSocket::bytesAvailable());
	RcTcpSocket *that = const_cast<RcTcpSocket *>(this);
	that->QTcpSocket::readData(buffer.data(), buffer.size());
	that->incoming += buffer;
    }
    return incoming.size();
}

bool RcTcpSocket::canTransferMore() const
{
    return !incoming.isEmpty() || QTcpSocket::bytesAvailable() > 0
        || !outgoing.isEmpty();
}

qint64 RcTcpSocket::readData(char *data, qint64 maxlen)
{
    int bytesRead = qMin<int>(maxlen, incoming.size());
    memcpy(data, incoming.constData(), bytesRead);
    incoming.remove(0, bytesRead);
    if (state() != ConnectedState) {
	QByteArray buffer;
	buffer.resize(QTcpSocket::bytesAvailable());
	QTcpSocket::readData(buffer.data(), buffer.size());
	incoming += buffer;
    }
    return qint64(bytesRead);
}

qint64 RcTcpSocket::readLineData(char *data, qint64 maxlen)
{
    return QIODevice::readLineData(data, maxlen);
}

qint64 RcTcpSocket::writeData(const char *data, qint64 len)
{
    int oldSize = outgoing.size();
    outgoing.resize(oldSize + len);
    memcpy(outgoing.data() + oldSize, data, len);
    emit readyToTransfer();
    return len;
}

void RateController::addSocket(RcTcpSocket *socket)
{
    connect(socket, SIGNAL(readyToTransfer()), this, SLOT(transfer()));
    socket->setReadBufferSize(downLimit * 2);
    sockets << socket;
    scheduleTransfer();
}

void RateController::removeSocket(RcTcpSocket *socket)
{
    disconnect(socket, SIGNAL(readyToTransfer()), this, SLOT(transfer()));
    socket->setReadBufferSize(0);
    sockets.remove(socket);
}

void RateController::setDownloadLimit(int bytesPerSecond)
{
    downLimit = bytesPerSecond;
    foreach (RcTcpSocket *socket, sockets)
	socket->setReadBufferSize(downLimit * 2);
}

void RateController::scheduleTransfer()
{
    if (transferScheduled)
        return;
    transferScheduled = true;
    QTimer::singleShot(50, this, SLOT(transfer()));
}

void RateController::transfer()
{
    transferScheduled = false;
    if (sockets.isEmpty())
	return;

    int msecs = 1000;
    if (!stopWatch.isNull())
        msecs = qMin(msecs, stopWatch.elapsed());

    qint64 bytesToWrite = (upLimit * msecs) / 1000;
    qint64 bytesToRead = (downLimit * msecs) / 1000;
    if (bytesToWrite == 0 && bytesToRead == 0) {
        scheduleTransfer();
        return;
    }

    QSet<RcTcpSocket *> pendingSockets;
    foreach (RcTcpSocket *client, sockets) {
        if (client->canTransferMore())
            pendingSockets << client;
    }
    if (pendingSockets.isEmpty())
        return;

    stopWatch.start();

    bool canTransferMore;
    do {
        canTransferMore = false;
        qint64 writeChunk = qMax<qint64>(1, bytesToWrite / pendingSockets.size());
        qint64 readChunk = qMax<qint64>(1, bytesToRead / pendingSockets.size());

        QSetIterator<RcTcpSocket *> it(pendingSockets);
        while (it.hasNext() && (bytesToWrite > 0 || bytesToRead > 0)) {
            RcTcpSocket *socket = it.next();

            bool dataTransferred = false;
            qint64 available = qMin<qint64>(socket->socketBytesAvailable(), readChunk);
            if (available > 0) {
                qint64 readBytes = socket->readFromSocket(qMin<qint64>(available, bytesToRead));
                if (readBytes > 0) {
                    bytesToRead -= readBytes;
                    dataTransferred = true;
                }
            }

            if (upLimit * 2 > socket->bytesToWrite()) {
                qint64 chunkSize = qMin<qint64>(writeChunk, bytesToWrite);
                qint64 toWrite = qMin(upLimit * 2 - socket->bytesToWrite(), chunkSize);
                if (toWrite > 0) {
                    qint64 writtenBytes = socket->writeToSocket(toWrite);
                    if (writtenBytes > 0) {
                        bytesToWrite -= writtenBytes;
                        dataTransferred = true;
                    }
                }
            }

            if (dataTransferred && socket->canTransferMore())
                canTransferMore = true;
            else
                pendingSockets.remove(socket);
        }
    } while (canTransferMore && (bytesToWrite > 0 || bytesToRead > 0) && !pendingSockets.isEmpty());

    if (canTransferMore)
	scheduleTransfer();
}
