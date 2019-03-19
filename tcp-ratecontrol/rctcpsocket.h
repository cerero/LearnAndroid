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

#ifndef RCTCPSOCKET_H
#define RCTCPSOCKET_H

#include <QtCore/QByteArray>
#include <QtCore/QSet>
#include <QtCore/QTime>
#include <QtNetwork/QTcpSocket>

class RcTcpSocket : public QTcpSocket
{
    Q_OBJECT
public:
    RcTcpSocket(QObject *parent = 0);

    inline bool canReadLine() const { return incoming.contains('\n'); }

    qint64 writeToSocket(qint64 maxlen);
    qint64 readFromSocket(qint64 maxlen);

    inline bool canTransferMore() const;
    qint64 bytesAvailable() const;
    inline qint64 socketBytesAvailable() const { return QTcpSocket::bytesAvailable(); }

signals:
    void readyToTransfer();

protected:
    qint64 readData(char *data, qint64 maxlen);
    qint64 readLineData(char *data, qint64 maxlen);
    qint64 writeData(const char *data, qint64 len);

private:
    QByteArray outgoing;
    QByteArray incoming;
};

class RateController : public QObject
{
    Q_OBJECT
public:
    inline RateController(QObject *parent = 0)
	: QObject(parent), transferScheduled(false) { }

    void addSocket(RcTcpSocket *socket);
    void removeSocket(RcTcpSocket *socket);

    inline int uploadLimit() const { return upLimit; }
    inline int downloadLimit() const { return downLimit; }
    inline void setUploadLimit(int bytesPerSecond) { upLimit = bytesPerSecond; }
    void setDownloadLimit(int bytesPerSecond);

public slots:
    void transfer();
    void scheduleTransfer();

private:
    QTime stopWatch;
    QSet<RcTcpSocket *> sockets;
    int upLimit;
    int downLimit;
    bool transferScheduled;
};

#endif
