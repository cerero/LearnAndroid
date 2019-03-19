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

#include <QtCore/QCoreApplication>
#include <QtCore/QDebug>

class Foo : public QObject
{
    Q_OBJECT
public slots:
    void showData()
    {
        RcTcpSocket *socket = qobject_cast<RcTcpSocket *>(sender());
        while (socket->canReadLine())
            qDebug() << socket->readLine();
    }
};

int main(int argc, char **argv)
{
    QCoreApplication app(argc, argv);

    RcTcpSocket socket;
    socket.connectToHost("www.trolltech.com", 80);
    socket.write("GET / HTTP/1.0\r\n\r\n");

    RateController controller;
    controller.setUploadLimit(512);
    controller.setDownloadLimit(2048);
    controller.addSocket(&socket);

    Foo foo;

    QObject::connect(&socket, SIGNAL(readyRead()), &foo, SLOT(showData()));

    return app.exec();
}

#include "main.moc"

