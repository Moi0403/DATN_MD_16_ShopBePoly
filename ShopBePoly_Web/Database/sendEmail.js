const nodemailer = require("nodemailer");

const sendEmail = async (to, subject, text) => {
  try {
    const transporter = nodemailer.createTransport({
      service: "gmail",
      auth: {
        user: "shoppepoly8386@gmail.com",       
        pass: "nidn zuvg qiso ytfj", 
      },
    });

    const mailOptions = {
      from: "shoppepoly8386@gmail.com",
      to,
      subject,
      text,
    };

    await transporter.sendMail(mailOptions);
    console.log("Email sent to:", to);
  } catch (error) {
    console.error("Failed to send email:", error);
    throw error;
  }
};

module.exports = sendEmail;
